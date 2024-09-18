package br.com.example.kellmertrack.services.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import br.com.example.kellmertrack.ACTION_BLUETOOTH_STATUS
import br.com.example.kellmertrack.ACTION_ROTACAO
import br.com.example.kellmertrack.BLAZONLABS
import br.com.example.kellmertrack.CONECTADO
import br.com.example.kellmertrack.DESCONECTADO
import br.com.example.kellmertrack.MECHATRONICS
import br.com.example.kellmertrack.PROCURANDO
import br.com.example.kellmertrack.R
import br.com.example.kellmertrack.TAG
import br.com.example.kellmertrack.local.model.entities.RotacaoEntity
import br.com.example.kellmertrack.local.repository.EntregaRepository
import br.com.example.kellmertrack.local.repository.RotacaoRepository
import br.com.example.kellmertrack.local.repository.SetupRepository
import br.com.example.kellmertrack.remote.service.FirebaseService
import br.com.example.kellmertrack.ui.notification.NotificationService
import br.com.example.kellmertrack.ui.utils.TipoNotificacao
import br.com.example.kellmertrack.ui.utils.showNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import javax.inject.Inject


@AndroidEntryPoint
@SuppressLint("MissingPermission")
class BluetoothService @Inject constructor(): Service(), BluetoothServiceCallback {

    @Inject
    lateinit var rotacaoRepository : RotacaoRepository
    @Inject
    lateinit var setupRepository : SetupRepository
    @Inject
    lateinit var firebaseService: FirebaseService
    @Inject
    lateinit var entregaRepository: EntregaRepository
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var wakeLock: PowerManager.WakeLock?=null

    private var context : Context = this
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var isScanning = false
    private var lastSensorDataScanned : Date? = null
    private val handler = Handler(Looper.getMainLooper())
    private val timer = Timer()
    private lateinit var bluetoothManager : BluetoothManager
    private val scope = CoroutineScope(Dispatchers.Main)


    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate() {
        super.onCreate()
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WAKELOCK").apply {
                acquire()
            }
        }
        startForegroundService()
        iniciaBluetooth()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null){
            when(intent.action){
                "START" -> {
                    //iniciaServices()
                }
                "STOP" -> disconnectDevice()
            }
        }
        return START_STICKY
    }

    private fun startForegroundService(){
        val notificationService = NotificationService(
            this,
            "Serviço do sensor iniciado",
            "Sensor",
            "Sensor"
        )
        startForeground(1, notificationService.createNotification())
    }

    override fun disconnectDevice() {
        bluetoothStatus = DESCONECTADO
        sendSensorStatusBroadcast()
        verificaConexaoDispositivo()
    }

    override fun onBind(intent: Intent): IBinder? {
        return LocalBinder()
    }

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    fun iniciaBluetooth() {
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Dispositivo não suporta Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        verificaConexaoDispositivo()
        if (bluetoothAdapter.isEnabled){
            scope.launch{
                if(setupRepository.buscaSetup() != null){
                    startBluetoothScanning()
                }
            }
        }
        timer.schedule(object : TimerTask() {
            override fun run() {
                val currentTime = Date().time
                Log.d(TAG, "================ rodando task: $bluetoothStatus, ${currentTime - (lastSensorDataScanned?.time ?: currentTime)}")
                if (bluetoothStatus == DESCONECTADO || (currentTime - (lastSensorDataScanned?.time ?: currentTime)) >= 60000){
                    if(!isScanning)
                        startBluetoothScanning()
                    bluetoothStatus = if(isScanning) PROCURANDO else DESCONECTADO
                    sendSensorStatusBroadcast()
                    verificaConexaoDispositivo()
                }

            }
        }, 30000, 30000)
    }

    override fun startBluetoothScanning() {
        val filters = mutableListOf<ScanFilter>()
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .build()

        scope.launch {
            val mac = setupRepository.buscaSetup()?.mac
            val tipoSensor = setupRepository.buscaSetup()?.modelo

            if (mac != null) {
                val filter = ScanFilter.Builder()
                    .setDeviceAddress(mac)
                    .build()
                filters.add(filter)

                startScan(filters, scanSettings, tipoSensor)
            }
        }
    }

    private fun startScan(filters: MutableList<ScanFilter>, scanSettings: ScanSettings, tipoSensor : String?) {
        val bluetoothLeScanner = bluetoothLeScanner
        isScanning = true
        bluetoothStatus = PROCURANDO
        sendSensorStatusBroadcast()
        verificaConexaoDispositivo()

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                Log.d(TAG, " $result")
                lastSensorDataScanned = Date()
                bluetoothStatus = CONECTADO
                sendSensorStatusBroadcast()
                verificaConexaoDispositivo()

                if (tipoSensor == MECHATRONICS) {

                    val timeDifference = lastSensorDataScanned!!.time - (lastSensorDataSaved?.time ?: 0)
                    if (timeDifference >= 30000) {
                        lastSensorDataSaved = Date()
                        getMechatronicData(result.scanRecord?.bytes)
                    }
                }else if(tipoSensor == BLAZONLABS){
                    val manufacturerSpecificData = result.scanRecord?.manufacturerSpecificData
                    manufacturerSpecificData?.let { dataMap ->
                        val manufacturerData = dataMap[89] // 89 é o código do fabricante
                        manufacturerData?.let { byteArray ->
                            val timeDifference = lastSensorDataScanned!!.time - (lastSensorDataSaved?.time ?: 0)
                            Log.d(TAG, "========= timeDifference: $timeDifference")
                            if (timeDifference >= 60000) {
                                lastSensorDataSaved = Date()
                                getBlazonlabsData(byteArray)
                            }
                        }
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                bluetoothLeScanner?.startScan(filters, scanSettings, scanCallback)
            }
        } else {
            bluetoothLeScanner?.startScan(filters, scanSettings, scanCallback)
        }
    }

    private fun verificaConexaoDispositivo(){
        when(bluetoothStatus){
            CONECTADO -> {
                chamaNotificacao(
                    "Sensor conectado",
                    "O sensor está conectado",
                    TipoNotificacao.ONLINE,
                    icon = R.drawable.ic_action_emoji_happy
                )
            }
            DESCONECTADO -> {
                chamaNotificacao(
                    "Sensor desconectado",
                    "O sensor não está conectado",
                    TipoNotificacao.OFFLINE,
                    icon = R.drawable.ic_action_emoji_sad
                )
            }
            PROCURANDO -> {
                chamaNotificacao(
                    "Sensor desconectado",
                    "Procurando sensor...",
                    TipoNotificacao.SCANNING,
                    icon = R.drawable.ic_action_emoji_neutral
                )
            }
        }
    }

    private fun chamaNotificacao(title:String, notificationText:String, type: TipoNotificacao = TipoNotificacao.NORMAL, channelName: String = "DEVICE_SERVICE", icon : Int){
        this.showNotification(1, title, notificationText, type, channelName, NotificationManager.IMPORTANCE_LOW, icon)
    }

    fun getMechatronicData(rawData: ByteArray?) {
        if (rawData != null){
            if (rawData.size < 30) {
                Log.e(TAG, "Raw data  muito curto!")
                return
            }
            val rotationDirection = rawData[7].toInt()
            val temperature = rawData[15].toInt()

            if(rotationDirection != 0){
                val rotacaoEntity = criaRotacaoEntity(0, temperature, rotationDirection)
                if (rotacaoEntity != null) {
                    sensorData = rotacaoEntity
                    sendSensorDataBroadcast(MECHATRONICS)
                    GlobalScope.launch {
                        rotacaoRepository.salvaDadosRotacao(rotacaoEntity)

                    }
                }
            }
        }
    }

    private fun getBlazonlabsData(byteArray: ByteArray){
        val hexString = byteArrayToHex(byteArray)
        // Verifica se a string hexadecimal possui 8 caracteres (4 pares)
        if (hexString.length != 8) {
            throw IllegalArgumentException("O hexadecimal deve ter 8 caracteres.")
        }
        val rpm = byteArray.copyOfRange(0, 1).joinToString("").toInt()
        val battery = hexString.substring(2, 4).toInt(16)
        val temperature = hexString.substring(4, 6).toInt(16)

        if(rpm != 0){
            val rotacaoEntity = criaRotacaoEntity(battery, temperature, rpm)
            if (rotacaoEntity != null) {
                sensorData = rotacaoEntity
                sendSensorDataBroadcast(BLAZONLABS)
                GlobalScope.launch {
                    rotacaoRepository.salvaDadosRotacao(rotacaoEntity)

                }
            }
        }
    }

    fun byteArrayToHex(byteArray: ByteArray): String {
        return byteArray.joinToString("") { String.format("%02X", it) }
    }

    fun criaRotacaoEntity(bateria : Int, temperatura : Int, direcao : Int): RotacaoEntity? {
        val setup = setupRepository.buscaSetup()
        return if (setup != null){
            val entrega = entregaRepository.findEntregaAtiva()
            val rotacao = RotacaoEntity(
                id = UUID.randomUUID().toString(),
                veiculoId = setup.veiculosId,
                dispositivo = setup.numeroInterno,
                rpm = if(setup.modelo == BLAZONLABS) direcao else 0,
                momento = Date(),
                entregaId = entrega?.id,
                bateria = bateria,
                temperatura = temperatura,
                direcao = direcao,
            )
            rotacao
        } else null
    }

    private fun sendSensorDataBroadcast(tipoSensor : String) {
        val intent = Intent(ACTION_ROTACAO).apply {
            if (tipoSensor == BLAZONLABS)
                this.putExtra(BLAZONLABS, "BLAZONLABS")
            else
                this.putExtra(MECHATRONICS, "MECHATRONICS")
        }
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun sendSensorStatusBroadcast() {
        val intent = Intent(ACTION_BLUETOOTH_STATUS).apply {
            this.putExtra("ACTION_BLUETOOTH_STATUS", getBluetoothStatus())
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.let {
            if (it.isHeld)
                it.release()
        }
        stopForeground(true)
        isScanning = false
    }

    companion object {
        private var sensorData: RotacaoEntity? = null
        fun getSensorData(): RotacaoEntity? {
            return sensorData
        }

        private var lastSensorDataSaved : Date? = null
        fun getLastSensorDataTime() : Date?{
            return lastSensorDataSaved
        }

        private var bluetoothStatus: String = DESCONECTADO
        fun getBluetoothStatus(): String {
            return bluetoothStatus
        }
    }
}
package br.com.example.kellmertrack.services.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.TRANSPORT_LE
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
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
import br.com.example.kellmertrack.extensions.isWritable
import br.com.example.kellmertrack.extensions.isWritableWithoutResponse
import br.com.example.kellmertrack.extensions.printGattTable
import br.com.example.kellmertrack.ui.BLUETOOTH_TAG
import br.com.example.kellmertrack.ui.notification.NotificationService
import br.com.example.kellmertrack.ui.utils.showNotification
import br.com.example.kellmertrack.local.model.entities.RotacaoEntity
import br.com.example.kellmertrack.local.model.mappers.RotacaoMapper
import br.com.example.kellmertrack.local.repository.EntregaRepository
import br.com.example.kellmertrack.local.repository.RotacaoRepository
import br.com.example.kellmertrack.local.repository.SetupRepository
import br.com.example.kellmertrack.remote.service.FirebaseService
import br.com.example.kellmertrack.ui.utils.TipoNotificacao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private var bluetoothGatt: BluetoothGatt? = null
    private var wakeLock: PowerManager.WakeLock?=null

    private var context : Context = this
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    //private var serviceStarted :Boolean = false
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
        //if (Build.VERSION.SDK_INT >= 31) {
        //if(checkPermissions()){
        //        iniciaBluetooth()
        //}
        //}else{
        iniciaBluetooth()
        //}
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

    private fun checkPermissions() : Boolean{
        return checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
    }

    override fun disconnectDevice() {
        //bluetoothGatt?.disconnect()
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

    /*private fun iniciaServices(){
        if(serviceStarted)
            return
        serviceStarted = true
    }*/

    fun iniciaBluetooth() {
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Dispositivo não suporta Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }
        verificaBluetooth()
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
            Log.d(TAG, "================ procurando pelo mac $mac ")
            val tipoSensor = setupRepository.buscaSetup()?.modelo

            if (mac != null) {
                val filter = ScanFilter.Builder()
                    .setDeviceAddress(mac)
                    .build()
                filters.add(filter)

                //if (dispositivoConectado == null) {
                    startScan(filters, scanSettings, tipoSensor)
                //}
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

                /*val rawData = result.scanRecord?.bytes
                if (rawData != null) {
                    // Converte os bytes para uma string hexadecimal
                    val rawDataHex = rawData.joinToString(separator = "") { byte -> String.format("%02X", byte) }
                    Log.d(TAG, "Raw Data: $rawDataHex")
                } else {
                    Log.d(TAG, "No raw data available")
                }*/

                if (tipoSensor == MECHATRONICS) {

                    val timeDifference = lastSensorDataScanned!!.time - (lastSensorDataSaved?.time ?: 0)
                    if (timeDifference >= 30000) {
                        lastSensorDataSaved = Date()
                        mecatronicsSensorData = result.scanRecord?.bytes
                        sendSensorDataBroadcast(MECHATRONICS)
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
                                blazonlabsSensorData = byteArray
                                sendSensorDataBroadcast(BLAZONLABS)
                            }
                        }
                    }
                }
            }
        }
        bluetoothLeScanner?.startScan(filters, scanSettings, scanCallback)
    }

    /*private fun dispositivosConectados(deviceAddress: String): BluetoothDevice? {
        val connectedDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)
        var dispositivo: BluetoothDevice? = null

        for (device in connectedDevices) {
            if (device.address == deviceAddress) {
                bluetoothStatus = CONECTADO
                sendSensorStatusBroadcast()
                dispositivo = device
            }
        }
        return dispositivo
    }

    private fun conectarDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, true, gattCallback, TRANSPORT_LE)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d(TAG, "onConnectionStateChange: newState: $newState")
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    handler.postDelayed({gatt?.discoverServices()},1000)
                    bluetoothStatus = CONECTADO
                    sendSensorStatusBroadcast()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    bluetoothStatus = DESCONECTADO
                    sendSensorStatusBroadcast()
                    //bluetoothGatt?.disconnect()
                    bluetoothGatt?.close()
                    handler.postDelayed({
                        bluetoothStatus = PROCURANDO
                        sendSensorStatusBroadcast()
                        verificaConexaoDispositivo()
                        startBluetoothScanning()
                        //val device = gatt?.device
                        //device?.let { conectarDevice(it) }
                    }, RECONNECTION_DELAY_MS.toLong())
                }
                else -> {
                    Log.d(BLUETOOTH_TAG, "Outro estado de conexão: $newState")
                }
            }
            verificaConexaoDispositivo()
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                /*Log.d(TAG, "============== onServicesDiscovered: Services")
                val services = gatt?.services
                services?.forEach { service ->
                    val serviceCharacteristic = service.characteristics
                    Log.d(TAG, "Service: ${service.uuid}")
                    Log.d(TAG, "Characteristics:")
                    serviceCharacteristic.forEach {
                        Log.d(TAG, it.uuid.toString())
                        Log.d(TAG, it.properties.toString())
                        Log.d(TAG, it.descriptors.toString())
                    }
                }*/
                val service = gatt?.getService(SERVICO_UUID)
                val characteristic = service?.getCharacteristic(CARACTERISTICA_UUID)

                gatt?.readCharacteristic(characteristic)
                readCharacteristic(characteristic)
                gatt?.setCharacteristicNotification(characteristic, true)
                enableNotifications(gatt, characteristic)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            val dataBytes = characteristic?.value
            val dataString = dataBytes?.toString(Charsets.UTF_8)

            val hexString: String = dataBytes?.joinToString(separator = " ") {
                String.format("%02X", it)
            }.toString()

            lastSensorDataSaved = Date()
            //blazonlabsSensorData = dataString
            sendSensorDataBroadcast(BLAZONLABS)

            scope.launch {
                val rpm = (dataString?.substringBefore(",")?.toInt() ?: 0)
                withContext(Dispatchers.Main){
                    if (setupRepository.buscaSetup() != null){
                        val entrega = entregaRepository.findEntregaAtiva()
                        val setup = setupRepository.buscaSetup()
                        val rotacao = RotacaoEntity(
                            id = UUID.randomUUID().toString(),
                            veiculoId = setup!!.veiculosId,
                            dispositivo = setup.numeroInterno,
                            rpm = rpm,
                            momento = Date(),
                            entregaId = entrega?.id
                        )
                        rotacaoRepository.salvaDadosRotacao(rotacao)
                        val rotacaoDTO = RotacaoMapper().fromRotacaoEntityToDTO(rotacao)
                        firebaseService.enviaInformacaoDispositivoBluetoothFirebase(rotacaoDTO)
                    }
                }
            }
            Log.d(BLUETOOTH_TAG, "ByteArray: $dataBytes hexString: $hexString dataString: $dataString valorDevice: $dataString")
        }
    }

    private fun readCharacteristic(characteristic: BluetoothGattCharacteristic?){
        if(bluetoothGatt != null && characteristic != null){
            val teste = bluetoothGatt?.readCharacteristic(characteristic)
            Log.d(TAG, "readCharacteristic-> característica encontrada: $teste")
        }else{
            Log.w(TAG, "BluetoothGatt not initialized")
        }
    }*/

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

    /*private fun enableNotifications(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        val descriptor = characteristic?.getDescriptor(
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        )
        descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        gatt?.writeDescriptor(descriptor)
    }*/

    private fun sendSensorDataBroadcast(tipoSensor : String) {
        val intent = Intent(ACTION_ROTACAO).apply {
            if (tipoSensor == BLAZONLABS)
                this.putExtra(BLAZONLABS, getBlazonlabsSensorData())
            else
                this.putExtra(MECHATRONICS, getMechatronicSensorData())
        }
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun sendSensorStatusBroadcast() {
        val intent = Intent(ACTION_BLUETOOTH_STATUS).apply {
            this.putExtra("ACTION_BLUETOOTH_STATUS", getBluetoothStatus())
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun verificaBluetooth(){
        if(!bluetoothAdapter.isEnabled){
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(enableBtIntent)
            }
            //bluetoothAdapter.enable()
        }
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
        private val SERVICO_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
        private val CARACTERISTICA_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
        private val RECONNECTION_DELAY_MS = 10000

        private var blazonlabsSensorData: ByteArray? = null
        fun getBlazonlabsSensorData(): ByteArray? {
            return blazonlabsSensorData
        }

        private var mecatronicsSensorData: ByteArray? = null
        fun getMechatronicSensorData(): ByteArray? {
            return mecatronicsSensorData
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
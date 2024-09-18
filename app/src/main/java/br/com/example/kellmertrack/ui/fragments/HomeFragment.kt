package br.com.example.kellmertrack.ui.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import br.com.example.kellmertrack.ACTION_BLUETOOTH_STATUS
import br.com.example.kellmertrack.ACTION_ROTACAO
import br.com.example.kellmertrack.BLAZONLABS
import br.com.example.kellmertrack.BuildConfig
import br.com.example.kellmertrack.CONECTADO
import br.com.example.kellmertrack.MECHATRONICS
import br.com.example.kellmertrack.PROCURANDO
import br.com.example.kellmertrack.R
import br.com.example.kellmertrack.TAG
import br.com.example.kellmertrack.core.Sistema
import br.com.example.kellmertrack.databinding.HomeFragmentBinding
import br.com.example.kellmertrack.extensions.verificaConexao
import br.com.example.kellmertrack.local.model.entities.RotacaoEntity
import br.com.example.kellmertrack.local.model.entities.SetupEntity
import br.com.example.kellmertrack.services.bluetooth.BluetoothService
import br.com.example.kellmertrack.services.location.LocationService
import br.com.example.kellmertrack.services.webSocket.WebSocketService
import br.com.example.kellmertrack.ui.MainActivity
import br.com.example.kellmertrack.ui.adapters.EventoAdapter
import br.com.example.kellmertrack.ui.viewmodel.AppViewModel
import br.com.example.kellmertrack.ui.viewmodel.ComponentesFragments
import br.com.example.kellmertrack.ui.viewmodel.HomeViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("MissingPermission")
class HomeFragment : Fragment() {
    private lateinit var binding : HomeFragmentBinding
    private lateinit var context : Context
    private lateinit var broadcastReceiver: DeviceServiceBroadcasterReceiver
    private val viewModel by viewModels<HomeViewModel>()
    private val appViewModel: AppViewModel by activityViewModels()
    private var isAppInForeground = false
    private var locationService: LocationService? = null
    private var bluetoothService: BluetoothService? = null
    private var webSocketService: WebSocketService? = null
    private val routerController by lazy { findNavController() }
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale("pt", "BR"))

    @Inject
    lateinit var eventosAdapter: EventoAdapter

    override fun onResume() {
        super.onResume()
        verificaStatus()
        iniciaBluetooth()
        startServices()
        isAppInForeground = true
        appViewModel.setBottomBar(ComponentesFragments(bottomBar = true))
    }

    override fun onPause() {
        super.onPause()
        isAppInForeground = false
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activity = requireActivity() as MainActivity
        activity.checkBasePermissions()
        verificaAtualizacaoApp()
        //inicializa()
        startServices()
        setBroadcastReceiver()
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    @SuppressLint("StringFormatInvalid")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = HomeFragmentBinding.bind(view)
        setObservers()
        inicializa()
        atualizaDadosSensor()
        atualizaSensorStatus()
        buscaInformacoesDispositivo()
    }

    //Serve para inicializar os serviços na primeira inicialização do aplicativo
    private fun startServices() {
        if (this.bluetoothService == null) {
           Intent(context, BluetoothService::class.java).also {
                it.action = "START"
                ContextCompat.startForegroundService(context, it)
                context.bindService(it, bluetoothServiceConnection, Context.BIND_AUTO_CREATE)
            }
        }


        Intent(context, LocationService::class.java).also {
            it.action = "START"
            ContextCompat.startForegroundService(context, it)
            context.bindService(it, locationServiceConnection, Context.BIND_AUTO_CREATE)
        }

        if(this.webSocketService == null){
            Intent(context, WebSocketService::class.java).also {
                it.action = "START"
                ContextCompat.startForegroundService(context, it)
                context.bindService(it, webSocketConnection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    private val locationServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.getService()
            viewModel.locationServiceStatus.postValue(true)
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            viewModel.locationServiceStatus.postValue(false)
        }
    }

    private val bluetoothServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as BluetoothService.LocalBinder
            bluetoothService = binder.getService()
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            viewModel.iniciaBluetooth.postValue(false)
        }
    }

    private val webSocketConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as WebSocketService.LocalBinder
            webSocketService = binder.getService()
            webSocketService!!.socketConnected.observe(this@HomeFragment) {
                viewModel.webSocketServiceStatus.postValue(it)
            }
        }

        override fun onServiceDisconnected(className: ComponentName?) {
        }
    }

    private fun iniciaBluetooth(){
        viewModel.iniciaBluetooth.observe(viewLifecycleOwner){ status ->
            if(status == false){
                bluetoothService?.iniciaBluetooth()
                viewModel.iniciaBluetooth.postValue(true)
            }
        }
    }

    private fun verificaAtualizacaoApp() {
        lifecycleScope.launch {
            viewModel.versao.observe(viewLifecycleOwner) { versao ->
                versao?.let { ver ->
                   if (ver.versionCode!! > BuildConfig.VERSION_CODE) {
                        val dialog = MaterialAlertDialogBuilder(requireContext())
                            .setCancelable(false)
                            .setView(R.layout.update_dialog)
                            .create()
                        dialog.show()
                        dialog.findViewById<Button>(R.id.btn_update_dialog_install)
                            ?.setOnClickListener {
                                val url = ver.uri
                                val i = Intent(Intent.ACTION_VIEW)
                                i.type = "application/vnd.android.package-archive"
                                i.data = Uri.parse(url)
                                startActivity(i)
                            }
                    }
                }
            }
        }
    }

    private fun buscaInformacoesDispositivo(){
        lifecycleScope.launch {
            viewModel.buscaSetup().apply {
                binding.tvCaminhao.text = "${this?.veiculosId}"
            }
        }
    }

    private fun verificaStatus() {
        val imgDispositivo = binding.imgHomeFragmentDispositivo
        val imgInternet = binding.imgHomeFragmentInternet
        val imgLocalizacao = binding.imgHomeFragmentLocalizacao
        val imgServidor = binding.imgHomeFragmentServidor

        setColorAndImage(imgInternet, verificaConexao(context))

        viewModel.conexaoBluetoothStatus.observe(viewLifecycleOwner){ status ->
            setColorAndImage(imgDispositivo, status)
        }

        viewModel.locationServiceStatus.observe(viewLifecycleOwner){ locationStatus->
            setColorAndImage(imgLocalizacao, locationStatus)
        }

        viewModel.webSocketServiceStatus.observe(viewLifecycleOwner){ socketStatus->
            setColorAndImage(imgServidor, socketStatus)
        }
    }

    private fun setColorAndImage(view : ImageView, status : Boolean){
        if(status){
            view.setImageResource(R.drawable.ic_check_circle)
            ImageViewCompat.setImageTintList(
                view,
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorSuccess))
            )
        }else{
            view.setImageResource(R.drawable.ic_warning)
            ImageViewCompat.setImageTintList(
                view,
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorDanger))
            )
        }
    }

    private fun atualizaSensorStatus() {
        activity?.runOnUiThread {
            lifecycleScope.launch {
                val bluetoothStatus = BluetoothService.getBluetoothStatus()
                if (bluetoothStatus == CONECTADO || bluetoothStatus == PROCURANDO ) {
                    if (bluetoothStatus == CONECTADO) {
                        binding.tvConexaoValue.text = "Conectado"
                        viewModel.conexaoBluetoothStatus.postValue(true)
                    } else {
                        binding.tvConexaoValue.text = "Procurando..."
                        viewModel.conexaoBluetoothStatus.postValue(false)
                    }
                } else {
                    binding.tvConexaoValue.text = "Desconectado"
                    viewModel.conexaoBluetoothStatus.postValue(false)
                }
            }
        }
    }

    private fun setBroadcastReceiver() {
        viewModel.broadcastReceiver.observe(viewLifecycleOwner){ status ->
            if(!status){
                println("------------------- setando broadcastReceiver")
                broadcastReceiver = DeviceServiceBroadcasterReceiver()
                val intentFilter = IntentFilter().apply {
                    this.addAction(ACTION_ROTACAO)
                    this.addAction(ACTION_BLUETOOTH_STATUS)
                }
                LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, intentFilter)
                viewModel.broadcastReceiver.postValue(true)
            }
        }
    }

    private inner class DeviceServiceBroadcasterReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_ROTACAO) {
                println("======================= broadcast recebido")
                //if(isAppInForeground){
                    GlobalScope.launch {
                        withContext(Dispatchers.Main) {
                            val intentExtra = intent.extras
                            if (intentExtra?.containsKey(BLAZONLABS) == true)
                                setSensorData(BluetoothService.getSensorData(), BLAZONLABS)
                                //atualizaDadosSensor()
                            else if (intentExtra?.containsKey(MECHATRONICS) == true)
                                setSensorData(BluetoothService.getSensorData(), MECHATRONICS)
                        }
                    }
                //}
            }else{
                atualizaSensorStatus()
            }
        }
    }

    fun setSensorData(rotacao : RotacaoEntity?, sensorModel : String) {
        with(binding){
            val momento = BluetoothService.getLastSensorDataTime()
            if(sensorModel == MECHATRONICS)
                tvRotationDirectionValue.text = rotacao?.direcao.toString()
            else if (sensorModel == BLAZONLABS){
                tvRpmValue.text = rotacao?.rpm.toString()
                tvBatteryValue.text = rotacao?.bateria.toString()
            }
            tvTemperatureValue.text = rotacao?.temperatura.toString()
            tvMomentoValue.text = "${momento?.let { dateFormat.format(it).toString() + "h"} ?: "Nenhuma conexão"}"
        }
    }

    private fun atualizaDadosSensor() {
        Log.d(TAG, "================================== atualizaDadosSensor chamado")
        val sensorModelo = viewModel.setup.value?.modelo ?: Sistema.getSetup()?.modelo
        Log.d(TAG, "================================== sensorModelo: ${sensorModelo}")
        if (sensorModelo != null){
            with(binding){
                if (sensorModelo == MECHATRONICS){
                    llBlazonlabsData.visibility = View.GONE
                    llMechatronicsData.visibility = View.VISIBLE
                } else if (sensorModelo == BLAZONLABS){
                    llBlazonlabsData.visibility = View.VISIBLE
                    llMechatronicsData.visibility = View.GONE
                }
            }
            buscaUltimosDados(sensorModelo)
        }
    }

    private fun buscaUltimosDados(modelo : String){
        lifecycleScope.launch {
            val dados = viewModel.buscaUltimosDados()
            Log.d(TAG, "buscaUltimosDados: ")
            Log.d(TAG, "$dados")
            binding.tvTemperatureValue.text = dados.temperatura.toString()
            binding.tvMomentoValue.text = dados.momento.toString()
            if (modelo == MECHATRONICS){
                binding.tvRotationDirectionValue.text = dados.direcao.toString()
            } else if (modelo == BLAZONLABS){
                binding.tvRpmValue.text = dados.direcao.toString()
                binding.tvBatteryValue.text = dados.bateria.toString()
            }
        }
    }

    private fun inicializa() {
        viewModel.setup.observe(viewLifecycleOwner) { dispositivo ->
            Log.d(TAG, "================================== dispositivo: $dispositivo")
            if (Sistema.getSetup() == null) {
                Sistema.configuraSistema(dispositivo)
                Log.d(TAG, "Sistema configurado: ${Sistema.getSetup()}")
                if (dispositivo == null) {
                    viewModel.logado.postValue(false)
                    routerController.navigate(R.id.action_HomeFragment_to_SetupFragment)
                } else {
                    viewModel.salvaLoginDispositivo(dispositivo)
                    viewModel.logado.postValue(true)
                    atualizaDadosSensor()
                    onResume()
                }
            }
        }
    }

    private fun setObservers(){
        binding.recyclerEventos.adapter = eventosAdapter
        viewModel.eventos.observe(viewLifecycleOwner) { eventos ->
            eventosAdapter.setEventos(eventos!!)
        }
    }
}
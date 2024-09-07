package br.com.example.kellmertrack.services.location

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.util.Log
import br.com.example.kellmertrack.R
import br.com.example.kellmertrack.TAG
import br.com.example.kellmertrack.TAG_TASK_DB
import br.com.example.kellmertrack.TAG_TASK_FIREBASE
import br.com.example.kellmertrack.TAG_TASK_UPDATE_APP
import br.com.example.kellmertrack.local.location.GeofenceTransition
import br.com.example.kellmertrack.local.location.KellmerTrackGeofence
import br.com.example.kellmertrack.services.tasks.TaskDBKellmertrack
import br.com.example.kellmertrack.ui.notification.NotificationService
import br.com.example.kellmertrack.ui.utils.showNotification
import br.com.grupohobi.kellmertrack.local.model.EventoService
import br.com.example.kellmertrack.local.model.TipoEvento
import br.com.example.kellmertrack.local.model.entities.TrajetoEntity
import br.com.example.kellmertrack.local.model.mappers.TrajetoMapper
import br.com.example.kellmertrack.local.repository.EntregaRepository
import br.com.example.kellmertrack.local.repository.SetupRepository
import br.com.example.kellmertrack.local.repository.TrajetoRepository
import br.com.example.kellmertrack.remote.service.FirebaseService
import br.com.example.kellmertrack.services.tasks.TaskCreator
import br.com.example.kellmertrack.services.tasks.TaskFirebase
import br.com.example.kellmertrack.services.tasks.TaskUpdateApp
import br.com.example.kellmertrack.ui.utils.TipoNotificacao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class LocationService @Inject constructor() : Service(), LocationClientCallBack {

    @Inject
    lateinit var trajetoRepository : TrajetoRepository
    @Inject
    lateinit var setupRepository : SetupRepository
    @Inject
    lateinit var entregaRepository: EntregaRepository
    @Inject
    lateinit var eventoService : EventoService

    @Inject
    lateinit var firebaseService : FirebaseService

    private val binder = LocalBinder()
    private var locationClient : LocationClient? = null
    private var kellmerTrackGeofence : KellmerTrackGeofence? = null
    private var geofenceIniciado = false

    override fun onCreate() {
        super.onCreate()
        val notificationService = NotificationService(this, "Serviço de localização iniciado","Location", "Location")
        startForeground(99, notificationService.createNotification())
        Log.d(TAG, "onCreate: locationClient: $locationClient")
        //if(locationClient == null)
            locationClient?.cleanLocationRequest()
            locationClient = LocationClient(this, this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null){
            when(intent.action){
                "START" -> {
                    Log.d(TAG, "onStartCommand: locationClient: $locationClient")
                    //if(locationClient == null)
                        locationClient?.cleanLocationRequest()
                        locationClient = LocationClient(this, this)
                    criaTasks()
                }
                "STOP" -> {
                    destroyService()
                }
            }
        }
        return START_STICKY
    }

    private fun destroyService() {
        stopForeground(true)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onLocationChanged(location: Location?) {
        Log.d(TAG, "onLocationChanged: chamado: $location")
        CoroutineScope(Dispatchers.Main).launch {
            try {
                location?.let {
                    if (lastLocation != null) {
                        Log.d("LocationService", "Distância = ${location.distanceTo(lastLocation!!)}")
                        if (location.distanceTo(lastLocation!!) > 0) {
                            val setup = setupRepository.buscaSetup()
                            if(setup != null){
                                val deviceId = setup.numeroInterno
                                val veiculoId = setup.veiculosId
                                val trajeto = criaTrajeto(location, deviceId, veiculoId)
                                val localizacaoDTO = TrajetoMapper().fromTrajetoEntityToDTO(trajeto)
                                monitorarGeofence(location)
                                trajetoRepository.salvaDadosTrajeto(trajeto)

                                firebaseService.enviaLocalizacaoFirebase(localizacaoDTO)
                            }
                        }
                    }
                    if (!geofenceIniciado)
                        iniciaGeofence()
                    lastLocation = location
                }
            } catch (e: Exception) {
                Log.d("LocationService", "Erro ao atualizar a localizacao: ${e.message.toString()}")
            }
        }
    }

    override fun onDestroy() {
        Log.d("destroy", "================= onDestroy chamado ")
        locationClient?.stopUpdatesLocation()
    }

    private fun criaTrajeto(location: Location, dispositivo : String, veiculoId : String): TrajetoEntity {
        return TrajetoEntity(
            momento = Date(),
            dispositivo = dispositivo,
            veiculoId = veiculoId,
            latitude = location.latitude,
            longitude = location.longitude,
            velocidade = (location.speed * 3.6).roundToInt(),
            sincronizado = false
        )
    }

    inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    private fun criaTasks() {
        val taskCreator = TaskCreator(applicationContext)
        taskCreator.periodicJob(TaskDBKellmertrack::class, TAG_TASK_DB, emptyMap(), 10, TimeUnit.HOURS)
        taskCreator.periodicJob(TaskFirebase::class, TAG_TASK_FIREBASE, emptyMap(), 15, TimeUnit.MINUTES)
        taskCreator.periodicJob(TaskUpdateApp::class, TAG_TASK_UPDATE_APP, emptyMap(), 1, TimeUnit.HOURS)
    }

    companion object{
        private var lastLocation:Location?=null

        fun getLastLocation(): Location? {
            return lastLocation
        }
    }

    private fun chamaNotificacao(title:String, notificationText:String){
        this.showNotification(
            22,
            title,
            notificationText,
            TipoNotificacao.NOCOLOR,
            "EVENTO",
            NotificationManager.IMPORTANCE_HIGH,
            icon = R.drawable.ic_action_location
        )
    }

    //GEOFENCE
    private suspend fun monitorarGeofence(location : Location){
        var geofenceResult = listOf<GeofenceTransition>()
        Log.d(TAG, "kellmertrackGeofence: ${kellmerTrackGeofence}")
        if(kellmerTrackGeofence != null){
            geofenceResult = kellmerTrackGeofence!!.novaLocalizacao(location)
        }
        Log.d(TAG, "geofenceResult: $geofenceResult")
        if (geofenceResult.isNotEmpty()){
            geofenceResult.forEach { transition ->
                val result = eventoService.novaTransicaoAsync(transition).await()
                println("monitorarGeofence: result -> $result")
                result?.let {
                    if(it.entregaId != "0") {
                        if (it.tipo == TipoEvento.ENTRADA){
                            chamaNotificacao(
                                "Chegou ao destino",
                                "Entrou na área da obra"
                            )
                        }else if (it.tipo == TipoEvento.SAIDA){
                            chamaNotificacao(
                                "Saiu da obra",
                                "Saiu da área da obra"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun iniciaGeofence(){
        val entregaAtiva = entregaRepository.findEntregaAtiva()
        if (entregaAtiva != null){
            CoroutineScope(Dispatchers.IO).launch {
                val entregas = entregaRepository.obterLocalizacoesEntregas()
                Log.d(TAG, "=============== entregas usadas para iniciar o genfence: $entregas")
                kellmerTrackGeofence = KellmerTrackGeofence(entregas, 200)
                geofenceIniciado = true
            }
        }
    }
}
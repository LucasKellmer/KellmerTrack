package br.com.example.kellmertrack.services.webSocket

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.example.kellmertrack.BuildConfig
import br.com.example.kellmertrack.R
import br.com.example.kellmertrack.ui.notification.NotificationService
import br.com.example.kellmertrack.ui.utils.showNotification
import br.com.example.kellmertrack.local.repository.EntregaRepository
import br.com.example.kellmertrack.local.repository.SetupRepository
import br.com.example.kellmertrack.services.tasks.TaskCreator
import br.com.example.kellmertrack.services.tasks.TaskEntrega
import br.com.example.kellmertrack.ui.utils.TipoNotificacao
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class WebSocketService @Inject constructor() : Service() {

    private lateinit var client: OkHttpClient
    private lateinit var webSocket: WebSocket
    private lateinit var webSocketListener: WebSocketListener

    @Inject
    lateinit var entregaRepository: EntregaRepository

    @Inject
    lateinit var taskCreator : TaskCreator

    @Inject
    lateinit var setupRepository: SetupRepository

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
    private var isClosed = false
    private val _socketConnected = MutableLiveData<Boolean>().apply { value = false }
    val socketConnected: LiveData<Boolean>
        get() = _socketConnected

    override fun onCreate() {
        super.onCreate()
        val notificationService = NotificationService(this, "Conexão com o servicor realizada","WebSocket", "WebSocket")
        startForeground(33, notificationService.createNotification())
        //connectWebSocket()
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (_socketConnected.value == false)
                    connectWebSocket()
            }
        }, 20000, 205000)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(intent != null){
            when(intent.action){
                "START" -> {
                    //connectWebSocket()
                }
                "STOP" -> {
                    destroyService()
                }
            }
        }
        return START_STICKY
    }

    private fun connectWebSocket() {
        val setup = setupRepository.buscaSetup()
        if(setup != null){
            val request = Request.Builder()
                .url("${BuildConfig.urlWs}?dispositivo=${setup.numeroInterno}")
                .build()

            client = OkHttpClient.Builder()
                .readTimeout(3, TimeUnit.SECONDS)
                .build()

            webSocketListener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d(TAG, "Conexão estabelecida.")
                    isClosed = false
                    chamaNotificacao(
                        33,
                        "KellmerTrack webSocket service",
                        "Conexão com o servidor disponível",
                        TipoNotificacao.ONLINE,
                        icon = R.drawable.ic_action_socket_on
                    )
                    _socketConnected.postValue(true)
                    webSocket.send("SOCKET CONECTADO")
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e(TAG, "Erro: ${t.message}")
                    isClosed = true
                    if (_socketConnected.value == null || _socketConnected.value == false){
                        chamaNotificacao(
                            33,
                            "KellmerTrack webSocket service",
                            "Conexão com o servidor indisponível",
                            TipoNotificacao.OFFLINE,
                            icon = R.drawable.ic_action_socket_off
                        )
                    }
                    _socketConnected.postValue(false)
                    webSocket.send("SOCKET DESCONECTADO")
                    reconectarSocket()
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.d(TAG, "Mensagem recebida: $text")
                    // [0] é o tipo, [1] é o entregaId
                    val message = text.split(";")
                    //val mesage = Gson().fromJson(text, WebSocketMessage::class.java)
                    recebeMensagemSocket(message)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "Conexão fechada.")
                    isClosed = true
                    if (_socketConnected.value == null || _socketConnected.value == false){
                        chamaNotificacao(
                            33,
                            "KellmerTrack webSocket service",
                            "Conexão com o servidor desativada",
                            TipoNotificacao.OFFLINE,
                            icon = R.drawable.ic_action_socket_off
                        )
                    }
                    _socketConnected.postValue(false)
                    webSocket.send("SOCKET DESCONECTADO")
                    reconectarSocket()
                }
            }
            webSocket = client.newWebSocket(request, webSocketListener)
        }
    }

    private fun reconectarSocket() {
        Handler(Looper.getMainLooper()).postDelayed({
            val setup = setupRepository.buscaSetup()
            if (_socketConnected.value == null || _socketConnected.value == false) {
                if(setup != null) {
                    val request = Request.Builder()
                        .url("${BuildConfig.urlWs}?dispositivo=${setup.numeroInterno}")
                        .build()
                    webSocket = client.newWebSocket(request, webSocketListener)
                }
            }
        }, 10000)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::webSocket.isInitialized)
            webSocket.close(1000, null)
    }

    private fun destroyService() {
        stopForeground(true)
        stopSelf()
    }

    inner class LocalBinder : Binder() {
        fun getService(): WebSocketService = this@WebSocketService
    }

    fun recebeMensagemSocket(message: List<String> ){
        Log.d(TAG, "recebeMensagemSocket: mensagem recebida: $message")
        if (message[0] == "CARREGAMENTO"){
            val entregaAtiva = entregaRepository.getEntregas()
            if (entregaAtiva.value.isNullOrEmpty()){
                Log.d(TAG, "entregaAtiva: ${entregaAtiva.value}")
                val entregaId = message[1]
                novaEntrega(entregaId)
            }
            Log.d(TAG, "ENTREGA DISPONÍVEL: ${message[1]}")
        }
    }

    private fun chamaNotificacao(serviceId: Int, title:String, notificationText:String, type: TipoNotificacao, channelName: String = "SOCKET_SERVICE", importance : Int = 2, icon : Int){
        this.showNotification(serviceId, title, notificationText, type, channelName, importance, icon)
    }

    private fun novaEntrega(entregaId : String){
        chamaNotificacao(
            11,
            "Nova Entrega",
            "Foi identificado uma nova entrega",
            TipoNotificacao.NOCOLOR,
            "ENTREGAS",
            NotificationManager.IMPORTANCE_HIGH,
            icon = R.drawable.ic_action_nova_entrega
        )
        val params = mapOf<String, Any>(
            "entregaId" to entregaId
        )
        taskCreator.uniqueRequest(
            TaskEntrega::class,
            "NOVA_ENTREGA",
            params,
        )
    }

    companion object {
        private const val TAG = "WebSocketService"
    }
}
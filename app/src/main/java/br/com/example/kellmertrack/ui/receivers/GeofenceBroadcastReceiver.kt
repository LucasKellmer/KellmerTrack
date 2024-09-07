package br.com.example.kellmertrack.ui.receivers

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import br.com.example.kellmertrack.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver(){

    override fun onReceive(context: Context, intent: Intent) {

        Log.d("GeofenceBroadcastReceiver", "intent.action : ${intent.action} ")
        Log.d("GeofenceReceiver", "intent recebido")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            Log.d("GeofenceReceiver", "Geofence error: ${geofencingEvent.errorCode}")
            return
        }

        // Lógica para lidar com a transição de Geofence (entrada/saída)
        val transitionType = geofencingEvent.geofenceTransition

        // Verifica o tipo de transição
        when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {                // Ação quando o dispositivo entra na área do Geofence
                Log.d("GeofenceReceiver", "Entrou na área do Geofence")
                // Você pode iniciar uma notificação, exibir uma mensagem, etc.
                createNotification(context, "Você ENTROU na área da entrega")
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                // Ação quando o dispositivo sai da área do Geofence
                Log.d("GeofenceReceiver", "Saiu da área do Geofence")
                // Você pode parar uma notificação, registrar a saída, etc.
                createNotification(context, "Você SAIU da área da entrega")
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                // Ação quando o dispositivo permanece na área do Geofence por um período de tempo
                Log.d("GeofenceReceiver", "Está dentro da área do Geofence")
                // Você pode atualizar informações, exibir uma mensagem, etc.
            }
            else -> {
                // Outros tipos de transição não são tratados neste exemplo
                Log.e("GeofenceReceiver", "Transição de Geofence desconhecida")
            }
        }
    }

    @SuppressLint("NewApi")
    fun createNotification(context: Context, message : String) {
        val channelName = "Geofence"
        val channel = NotificationChannel(channelName, channelName, NotificationManager.IMPORTANCE_LOW)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(context, channelName)
            .setSmallIcon(R.drawable.ic_check_circle)
            .setAutoCancel(false)
            .setColorized(true)
            .setContentTitle(message)
            .setContentText("teste teste teste teste")

        notificationBuilder.color = ContextCompat.getColor(context, R.color.colorInfo)
        manager.notify(2, notificationBuilder.build())
    }
}
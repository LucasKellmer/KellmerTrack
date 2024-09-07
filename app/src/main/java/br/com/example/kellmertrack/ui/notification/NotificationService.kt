package br.com.example.kellmertrack.ui.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import br.com.example.kellmertrack.R

class NotificationService(
    private val context: Context,
    private val content: String?,
    private val channelId : String,
    private val channelName : String
) {

    init {
        createNotificationChannel()
        createNotification()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                lightColor = Color.GREEN
                enableLights(true)
                enableVibration(false)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun createNotification(): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setContentTitle("Serviço KellmerTrack")
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setDefaults(0)
            .setColorized(true)
            .setSound(null)
            .build()
    }
}
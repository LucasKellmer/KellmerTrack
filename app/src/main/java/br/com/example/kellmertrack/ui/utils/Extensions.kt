package br.com.example.kellmertrack.ui.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import br.com.example.kellmertrack.R

fun Service.showNotification(serviceId: Int, title:String, notificationText:String, type: TipoNotificacao = TipoNotificacao.NORMAL, channelName: String = "SENSOR_SERVICES", importance : Int, icon : Int){
    val channel = NotificationChannel(channelName, channelName, importance)
    val notificationService = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
    notificationService.createNotificationChannel(channel)

    val notificationBuilder = NotificationCompat.Builder(this, channelName)
        .setSmallIcon(icon)
        .setAutoCancel(false)
        .setColorized(true)

    notificationBuilder.setStyle(
        NotificationCompat.BigTextStyle()
            .setBigContentTitle(title)
            .bigText(notificationText)
    )

    notificationBuilder.color = when(type){
        TipoNotificacao.NORMAL -> ContextCompat.getColor(applicationContext, R.color.colorInfo)
        TipoNotificacao.ONLINE -> ContextCompat.getColor(applicationContext, R.color.colorSuccess)
        TipoNotificacao.OFFLINE -> ContextCompat.getColor(applicationContext, R.color.colorAccent)
        TipoNotificacao.SCANNING -> ContextCompat.getColor(applicationContext, R.color.colorAttention)
        TipoNotificacao.NOCOLOR -> ContextCompat.getColor(applicationContext, R.color.transparent)
    }
    notificationService.notify(serviceId, notificationBuilder.build())
}
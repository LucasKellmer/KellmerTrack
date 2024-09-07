package br.com.example.kellmertrack.ui.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.com.example.kellmertrack.services.location.LocationService

class LocationRebootedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            val locationIntent = Intent(context, LocationService::class.java)
            context?.startForegroundService(locationIntent)
        }
    }
}
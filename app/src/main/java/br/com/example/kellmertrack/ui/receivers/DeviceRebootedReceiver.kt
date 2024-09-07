package br.com.example.kellmertrack.ui.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.com.example.kellmertrack.services.bluetooth.BluetoothService

class DeviceRebootedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            val deviceIntent = Intent(context, BluetoothService::class.java)
            context?.startForegroundService(deviceIntent)
        }
    }
}
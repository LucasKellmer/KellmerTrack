package br.com.example.kellmertrack.ui.utils

import android.content.Context
import android.net.ConnectivityManager
import android.os.BatteryManager
import android.os.PowerManager
import android.provider.Settings
import br.com.example.kellmertrack.BuildConfig
import br.com.example.kellmertrack.core.Sistema
import br.com.example.kellmertrack.local.repository.SetupRepository
import br.com.example.kellmertrack.remote.model.DispositivoStatus
import br.com.example.kellmertrack.services.bluetooth.BluetoothService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject

class DispositivoFunctions @Inject constructor(
    @ApplicationContext private val context: Context,
    private val setupRepository: SetupRepository,
) {

    fun criaDispositivoStatus() : DispositivoStatus? {
        val bateryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val setup = Sistema.getSetup() ?: setupRepository.buscaSetup()
        setup?.let {
            return DispositivoStatus(
                BuildConfig.VERSION_NAME,
                it.numeroInterno,
                bateryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY),
                Date(),
                powerManager.isPowerSaveMode,
                BluetoothService.getBluetoothStatus()
            )
        }
        return null
    }
}
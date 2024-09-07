package br.com.example.kellmertrack.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import br.com.example.kellmertrack.R
import br.com.example.kellmertrack.databinding.ActivityMainBinding
import br.com.example.kellmertrack.ui.fragments.FuncoesInativasFragment
import br.com.example.kellmertrack.ui.viewmodel.AppViewModel
import br.com.example.kellmertrack.ui.viewmodel.ComponentesFragments
import dagger.hilt.android.AndroidEntryPoint

const val BLUETOOTH_TAG = "Bluetooth"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val routerController by lazy { findNavController(R.id.mainFragment) }
    private val appViewModel by viewModels<AppViewModel>()
    private var locationStatus: Boolean = false
    private var powerSaveStatus : Boolean = false
    private var bluetoothStatus : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        configRouteController()
        routerController.navigate(R.id.HomeFragment)
        bottomBarObserve()
    }

    override fun onResume() {
        super.onResume()
        checkDeviceFunctions()
    }

    private fun bottomBarObserve() {
        appViewModel.componentes.observe(this, Observer {
            it?.let { componentes ->
                if (componentes.bottomBar) {
                    binding.bottomNavigationView.visibility = VISIBLE
                } else {
                    binding.bottomNavigationView.visibility = GONE
                }
            }
        })
    }

    private fun showInactiveFunctionsDialog() {
        val fragmentManager = supportFragmentManager
        val dialogFragment = FuncoesInativasFragment()
        dialogFragment.isCancelable = false
        dialogFragment.show(fragmentManager, "FuncoesInativasFragment")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    fun checkDeviceFunctions(){
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        powerSaveStatus = powerManager.isPowerSaveMode

        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        bluetoothStatus = bluetoothAdapter?.isEnabled == true

        if (!locationStatus || powerSaveStatus || !bluetoothStatus){
            showInactiveFunctionsDialog()//routerController.navigate(R.id.funcoesInativasFragment)
        }
    }

    private fun configRouteController() {
        appViewModel.setBottomBar(ComponentesFragments(bottomBar = true))
        NavigationUI.setupWithNavController(binding.bottomNavigationView, routerController)
    }

    fun checkBasePermissions(): Boolean {
        val permissionsToRequest = ArrayList<String>()

        if (Build.VERSION.SDK_INT >= 31) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)

            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if(Build.VERSION.SDK_INT >= 33){
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)


        if (permissionsToRequest.isNotEmpty())
            requestPermissions(permissionsToRequest.toTypedArray(), 1)
        else
            checkBackgroundLocationPermissions()

        return permissionsToRequest.isEmpty()
    }

    private fun checkBackgroundLocationPermissions(): Boolean {
        val permissionsToRequest = ArrayList<String>()
        if (Build.VERSION.SDK_INT >= 29) {
            if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        if (permissionsToRequest.isNotEmpty())
            requestPermissions(permissionsToRequest.toTypedArray(), 1)
        return permissionsToRequest.isEmpty()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) {
                if(requestCode == 1)
                    checkBasePermissions()
                else if(requestCode == 2)
                    checkBackgroundLocationPermissions()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}
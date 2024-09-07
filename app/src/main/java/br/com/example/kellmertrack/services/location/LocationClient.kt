package br.com.example.kellmertrack.services.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import br.com.example.kellmertrack.extensions.toText
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import javax.inject.Inject

class LocationClient @Inject constructor(
    private val context: Context,
    private val locationClientCallBack: LocationClientCallBack
) {

    private var locationResult: Location? = null
    private var locationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    init {
        init()
    }

    @SuppressLint("MissingPermission")

    private fun init() {
        if(locationCallback != null || locationClient != null)
            cleanLocationRequest()

        val locationRequest = LocationRequest.create().apply {
            isWaitForAccurateLocation = false                   // não espera for um localizacao precisa antes de gravar
            smallestDisplacement = 3F                           // distancia minima para atualizacao: 3 metros
            interval = 10000                                    // intervalo mínimo para cada atualizaçãoo: 10 segundos
            fastestInterval = 10000                             // o aplicativo está preparado para receber atualizações: 10 segundos
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY   // indica que o aplicativo vai usar o GPS da forma mais precisa possível
        }

        locationClient = LocationServices.getFusedLocationProviderClient(context)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                locationResult = result.lastLocation
                Log.d("LocationClient", "${result.lastLocation.toText()}")
                locationClientCallBack.onLocationChanged(result.lastLocation)
            }
        }
        locationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    fun stopUpdatesLocation() {
        locationClient?.removeLocationUpdates(locationCallback!!)
    }

    fun cleanLocationRequest(){
        if(locationCallback != null)
            locationClient?.removeLocationUpdates(locationCallback!!)
        locationCallback= null
        locationClient= null
    }

    fun removeLocationUpdates(){
        cleanLocationRequest()
    }
}
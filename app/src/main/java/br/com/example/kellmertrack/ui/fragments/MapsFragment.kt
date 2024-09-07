package br.com.example.kellmertrack.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import br.com.example.kellmertrack.R
import br.com.example.kellmertrack.core.Sistema
import br.com.example.kellmertrack.ui.viewmodel.MapsViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

const val CAMERA_ZOOM_LEVEL = 17f

@AndroidEntryPoint
@SuppressLint("MissingPermission")
class MapsFragment : Fragment() {

    private lateinit var context : Context
    private lateinit var map : GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val viewModel by viewModels<MapsViewModel>()

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        if(!isLocationPermissionGranted()){
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            //faltou aqui
        }else{
            map.isMyLocationEnabled = true

            //get last know location data
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null){
                    with(map){
                        val latLng = LatLng(it.latitude, it.longitude)
                        moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, CAMERA_ZOOM_LEVEL))
                    }
                }else{
                    with(map){
                        moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(65.01355297927051, 25.46019811372978),
                                CAMERA_ZOOM_LEVEL
                            )
                        )
                    }
                }
            }
        }
        lifecycleScope.launch {
            val veiculo = Sistema.getSetup()?.veiculosId
            val entrega = viewModel.getSoEntregas()
            val empresa = viewModel.getEmpresa()
            Log.d("entrega", "buscaEntrega: ${entrega}")
            println(veiculo)
            println(entrega)
            viewModel.criaMarcacoes(entrega, map, empresa, context)
        }
        /*val sydney = LatLng(37.4220, -122.0841)
        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map.addCircle(
            CircleOptions()
                .center(sydney)
                .strokeColor(Color.argb(50, 70, 70, 70))
                .radius(100.0)
        )*/
        //map.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    private fun isLocationPermissionGranted() : Boolean{
        return checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
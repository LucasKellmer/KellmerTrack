package br.com.example.kellmertrack.extensions

import android.location.Location
import com.google.android.gms.maps.model.LatLng

fun Location?.toText(): String? {
    return if (this != null) "Lat: $latitude, Lon: $longitude" else "Localização desconhecida"
}

/*fun Location.toLatLng(): LatLng? {
    return if (this != null) LatLng(this?.latitude as Double, this?.longitude as Double) else null
}

fun Location.ofLatLng(latLng: LatLng): Location? {
    this.latitude = latLng.latitude
    this.longitude = latLng.longitude
    return this
}*/


package br.com.example.kellmertrack.extensions

import android.location.Location
import br.com.example.kellmertrack.BuildConfig

fun Location?.toText(): String? {
    return if (this != null) "Lat: $latitude, Lon: $longitude" else "Localização desconhecida"
}

fun Location.healthy():Boolean{
    if (this.hasAccuracy() && this.accuracy > 70)
        return false
    if (this.hasSpeed() && !BuildConfig.DEBUG) {
        val kmh = (this.speed * 3.6)
        if (kmh < 1.5)
            return false
    }
    return true
}

package br.com.example.kellmertrack.local.location

import br.com.example.kellmertrack.local.model.TipoEvento
import com.google.android.gms.maps.model.LatLng
import java.util.Date

data class GeofenceTransition(val tipo: TipoEvento, val entregaId:String, val location: LatLng, val momento: Date) {
}
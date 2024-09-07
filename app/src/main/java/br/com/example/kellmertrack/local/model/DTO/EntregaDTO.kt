package br.com.example.kellmertrack.local.model.DTO

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class EntregaDTO(
    val entregaId : Int,
    var recebedor : String? = null,
    val momento : Date,
    var quantidade : Double? = null,
    var latitude : Double? = null,
    var longitude : Double? = null,
    var assinatura : String? = null
) : Parcelable {
}
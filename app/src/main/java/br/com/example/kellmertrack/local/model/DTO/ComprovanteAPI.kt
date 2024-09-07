package br.com.example.kellmertrack.local.model.DTO

import okhttp3.MultipartBody
import java.util.Date

data class ComprovanteAPI(
    val id : String?,
    val entregaId : Int,
    //val contrato : String,
    val momento : Date,
    val latitude : Double?,
    val longitude : Double?,
    val recebedor : String?,
    var imgComprovante : ByteArray?,
) {
}
package br.com.example.kellmertrack.local.model.DTO

import okhttp3.MultipartBody

data class ComprovanteDTO(
    val id : String?,
    val contrato : String?,
    val entregaId : Int,
    val cliente : String?,
    val obra : String?,
    val quantidade : String,
    val recebedor : String?,
    val assinatura : ByteArray?,
    var uriComprovante : String?,
    var imgComprovante : ByteArray?
) {
}
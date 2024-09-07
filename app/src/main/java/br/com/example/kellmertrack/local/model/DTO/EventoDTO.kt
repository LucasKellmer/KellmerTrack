package br.com.example.kellmertrack.local.model.DTO

import java.util.Date

data class EventoDTO (
    val id:String?,
    val entregaId:String?,
    val contrato: String?,
    var momento: Date?,
    var latitude:Double?,
    var longitude:Double?,
    var veiculo : String?,
    val tipo:String,
    var texto:String,
){

}
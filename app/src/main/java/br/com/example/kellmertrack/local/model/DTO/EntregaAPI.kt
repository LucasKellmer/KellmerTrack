package br.com.example.kellmertrack.local.model.DTO

import java.util.Date

data class EntregaAPI(
    val id : Int,
    val momento: Date,
    val veiculo : String,
    //val obra : ObraDTO,
    val contrato : ContratoDTO,
    val status : Int?,
    val quantidade : Double,
    val dataSaidaObra : Date?,
    val dataEntradaObra : Date?,
    val dataSaidaUsina : Date?,
    val dataEntradaUsina : Date?,
    //var sincronizado: Boolean = false,
)
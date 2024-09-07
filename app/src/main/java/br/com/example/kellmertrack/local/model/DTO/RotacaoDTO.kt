package br.com.example.kellmertrack.local.model.DTO

import java.util.Date

data class RotacaoDTO (
    val id : String,
    val dispositivo : String,
    val veiculo : String,
    val rpm : Int,
    val momento : Date,
    val entregaId : Int?,
    val bateria : Int?,
    val temperatura : Int?,
    val direcao : Int?
        ){
}
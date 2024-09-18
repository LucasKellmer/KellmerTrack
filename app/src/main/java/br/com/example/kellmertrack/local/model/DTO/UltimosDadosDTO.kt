package br.com.example.kellmertrack.local.model.DTO

import java.util.Date

data class UltimosDadosDTO (
    val direcao : Int?,
    val momento : Date?,
    val temperatura : Int?,
    val bateria : Int?,
)
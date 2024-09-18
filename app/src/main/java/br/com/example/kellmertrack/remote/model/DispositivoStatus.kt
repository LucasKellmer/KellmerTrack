package br.com.example.kellmertrack.remote.model

import java.util.Date

data class DispositivoStatus(
    val versao : String,
    val dispositivo : String,
    val bateria : Int,
    val momento : Date,
    val economiaEnergia : Boolean,
    val sensorStatus : String?
)
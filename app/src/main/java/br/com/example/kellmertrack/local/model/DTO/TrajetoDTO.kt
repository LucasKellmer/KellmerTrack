package br.com.example.kellmertrack.local.model.DTO

import java.util.Date
import java.util.UUID

data class TrajetoDTO (
        val id: String = UUID.randomUUID().toString(),
        val dispositivo: String,
        val veiculoId: String,
        val momento: Date,
        val latitude: Double,
        val longitude: Double,
        val velocidade: Int,
        ){
}
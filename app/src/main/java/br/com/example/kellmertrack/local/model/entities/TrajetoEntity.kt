package br.com.example.kellmertrack.local.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "trajeto")
data class TrajetoEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString(),
    val dispositivo: String,
    val veiculoId: String,
    val momento: Date,
    val latitude: Double,
    val longitude: Double,
    val velocidade: Int,
    var sincronizado: Boolean = false,
)
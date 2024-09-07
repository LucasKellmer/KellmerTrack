package br.com.example.kellmertrack.local.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "empresa")
data class EmpresaEntity(
    @PrimaryKey
    val codigo : String,
    val nome : String,
    val latitude : Double,
    val longitude : Double,
    val raio : Double
) {
}
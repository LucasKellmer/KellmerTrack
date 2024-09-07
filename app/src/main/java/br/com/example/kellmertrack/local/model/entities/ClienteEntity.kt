package br.com.example.kellmertrack.local.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey
    val id : Int,
    val nome : String,
    val cpf : String,
    val cnpj : String?,
    val email : String?,
) {
}
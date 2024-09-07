package br.com.example.kellmertrack.local.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "obra")
data class ObraEntity (
    @PrimaryKey
    val id : Int,
    val descricao : String,
    val cidade : String,
    val bairro : String,
    val numero : String,
    val latitude : Double,
    val longitude : Double
){
}
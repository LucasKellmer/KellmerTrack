package br.com.example.kellmertrack.local.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "rotacao")
data class RotacaoEntity (
    @PrimaryKey(autoGenerate = false)
    var id: String = UUID.randomUUID().toString(),
    val dispositivo : String,
    var veiculoId : String,
    var momento : Date,
    var rpm : Int,
    var entregaId : Int?,
    val bateria : Int?,
    val temperatura : Int?,
    val direcao : Int?,
    var sincronizado : Boolean = false,
){
}
package br.com.example.kellmertrack.local.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contratos")
data class ContratoEntity (
    @PrimaryKey
    val numero: String,
    val empresa : String,
    //@ColumnInfo(name = "obra_id")
    val obraId : Int,
    val cliente :Int,
) {
}
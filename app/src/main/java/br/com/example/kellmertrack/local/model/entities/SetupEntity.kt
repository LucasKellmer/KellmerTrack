package br.com.example.kellmertrack.local.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "setup")
data class SetupEntity (
    @PrimaryKey
    val id:Int? = 1,
    @ColumnInfo(name = "numero_interno")
    val numeroInterno : String,
    @ColumnInfo(name = "veiculo_id")
    val veiculosId : String,
    val mac : String,
    val empresa : String,
    val modelo : String,
    ){
}
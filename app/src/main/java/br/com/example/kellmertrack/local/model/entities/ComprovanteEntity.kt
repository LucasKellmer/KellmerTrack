package br.com.example.kellmertrack.local.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "entrega_comprovante")
data class ComprovanteEntity(
    @PrimaryKey(autoGenerate = false)
    val id : String,
    @ColumnInfo(name = "entrega_id")
    val entregaId : Int,
    val momento : Date,
    val latitude : Double?,
    val longitude : Double?,
    val recebedor : String?,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var assinatura:ByteArray?,
    @ColumnInfo(name = "uri_comprovante")
    var uriComprovante : String?,
    @ColumnInfo(name = "img_comprovante", typeAffinity = ColumnInfo.BLOB)
    var imgComprovante : ByteArray?,
    val sincronizado : Boolean? = false
) {
}
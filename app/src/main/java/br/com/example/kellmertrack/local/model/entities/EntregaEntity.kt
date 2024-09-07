package br.com.example.kellmertrack.local.model.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Entity(tableName = "entrega")
@Parcelize
data class
EntregaEntity (
    @PrimaryKey(autoGenerate = false)
    val id : Int? = null,
    val momento: Date,
    val veiculo : String,
    val status : Int?,
    val quantidade : Double,
    val contrato : String,
    @ColumnInfo(name = "quantidade_entregue")
    val quantidadeEntregue : Double?,
    @ColumnInfo(name = "data_saida_obra")
    val dataSaidaObra : Date?,
    @ColumnInfo(name = "data_entrada_obra")
    val dataEntradaObra : Date?,
    @ColumnInfo(name = "data_saida_usina")
    val dataSaidaUsina : Date?,
    @ColumnInfo(name = "data_entrada_usina")
    val dataEntradaUsina : Date?,
    var sincronizado: Boolean = false,
) : Parcelable{
}
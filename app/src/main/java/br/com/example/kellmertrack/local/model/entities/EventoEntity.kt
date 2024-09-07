package br.com.example.kellmertrack.local.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import br.com.example.kellmertrack.local.model.TipoEvento
import java.util.Date
import java.util.UUID

@Entity(tableName = "eventos")
data class EventoEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name="entrega_id")
    val entregaId:String,
    val contrato : String?,
    var momento: Date?,
    var latitude:Double?,
    var longitude:Double?,
    val tipo:String,
    var texto:String,
    //@ColumnInfo(name="notificacao_visualizada")
    var notificacaoVisualizada:Boolean=false,
    var sincronizado : Boolean = false
) {

    companion object {
        fun createBasicEvent(entregaId: String, contrato : String?, tipo: TipoEvento): EventoEntity {
            return EventoEntity(UUID.randomUUID().toString(), entregaId, contrato, Date(), null, null, tipo.tipo, "")
        }
    }
}
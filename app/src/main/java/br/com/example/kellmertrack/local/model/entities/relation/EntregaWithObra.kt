package br.com.example.kellmertrack.local.model.entities.relation

import androidx.room.Embedded
import androidx.room.Relation
import br.com.example.kellmertrack.local.model.entities.ContratoEntity
import br.com.example.kellmertrack.local.model.entities.EntregaEntity

data class EntregaWithObra (

    @Embedded
    val entregaEntity: EntregaEntity?,

    @Relation(
        entity = ContratoEntity::class,
        parentColumn = "contrato",
        entityColumn = "numero"
    )
    val contratoEntity: ContratoWithObraAndCliente
    /*@Relation(
        parentColumn = "obra_id",
        entityColumn = "id"
    )
    val obraEntity : ObraEntity?*/
){
}
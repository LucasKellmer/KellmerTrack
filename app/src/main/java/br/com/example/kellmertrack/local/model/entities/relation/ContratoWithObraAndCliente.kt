package br.com.example.kellmertrack.local.model.entities.relation

import androidx.room.Embedded
import androidx.room.Relation
import br.com.example.kellmertrack.local.model.entities.ClienteEntity
import br.com.example.kellmertrack.local.model.entities.ContratoEntity
import br.com.example.kellmertrack.local.model.entities.ObraEntity

data class ContratoWithObraAndCliente(
    @Embedded
    val contratoEntity: ContratoEntity?,

    @Relation(
        parentColumn = "obraId",
        entityColumn = "id"
    )
    val obraEntity: ObraEntity?,

    @Relation(
    parentColumn = "cliente",
    entityColumn = "id"
    )
    val clienteEntity: ClienteEntity
) {
}
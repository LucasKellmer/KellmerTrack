package br.com.example.kellmertrack.local.model.mappers

import br.com.example.kellmertrack.local.model.DTO.ContratoDTO
import br.com.example.kellmertrack.local.model.entities.ContratoEntity

class ContratoMapper {

    fun toContratoEntity(contrato: ContratoDTO): ContratoEntity {
        return ContratoEntity(
            numero = contrato.numero,
            empresa = contrato.empresa,
            cliente = contrato.cliente.id,
            obraId = contrato.obra.id
        )
    }
}
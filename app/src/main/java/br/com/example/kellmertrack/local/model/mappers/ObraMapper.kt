package br.com.example.kellmertrack.local.model.mappers

import br.com.example.kellmertrack.local.model.DTO.ObraDTO
import br.com.example.kellmertrack.local.model.entities.ObraEntity

class ObraMapper {

    fun toObraEntity(obra : ObraDTO) : ObraEntity {
        return ObraEntity(
            id = obra.id,
            descricao = obra.descricao,
            cidade = obra.cidade,
            bairro = obra.bairro,
            numero = obra.numero.toString(),
            latitude = obra.latitude,
            longitude = obra.longitude,
        )
    }
}
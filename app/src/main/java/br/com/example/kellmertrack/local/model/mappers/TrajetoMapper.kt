
package br.com.example.kellmertrack.local.model.mappers

import br.com.example.kellmertrack.local.model.DTO.TrajetoDTO
import br.com.example.kellmertrack.local.model.entities.TrajetoEntity

class TrajetoMapper {

    fun fromTrajetoEntityToDTO(trajeto: TrajetoEntity) : TrajetoDTO {
        return TrajetoDTO(
            id = trajeto.id,
            veiculoId = trajeto.veiculoId,
            dispositivo = trajeto.dispositivo,
            velocidade = trajeto.velocidade,
            momento = trajeto.momento,
            latitude = trajeto.latitude,
            longitude = trajeto.longitude
        )
    }
}
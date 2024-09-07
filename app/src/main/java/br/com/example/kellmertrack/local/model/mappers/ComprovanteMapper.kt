package br.com.example.kellmertrack.local.model.mappers

import br.com.example.kellmertrack.local.model.DTO.ComprovanteAPI
import br.com.example.kellmertrack.local.model.DTO.ComprovanteDTO
import br.com.example.kellmertrack.local.model.entities.ComprovanteEntity
import br.com.example.kellmertrack.services.location.LocationService
import java.util.Date
import java.util.UUID

class ComprovanteMapper {

    fun fromDtoToEntity(dto: ComprovanteDTO) : ComprovanteEntity{
        val ultimaLocalizacao = LocationService.getLastLocation()
        return ComprovanteEntity(
            id = dto.id ?: UUID.randomUUID().toString(),
            entregaId = dto.entregaId,
            momento = Date(),
            latitude = ultimaLocalizacao?.latitude,
            longitude = ultimaLocalizacao?.longitude,
            recebedor = dto.recebedor,
            assinatura = dto.assinatura,
            uriComprovante = dto.uriComprovante,
            imgComprovante = null,
            sincronizado = false
        )
    }

    fun fromEntityToApi(entity: ComprovanteEntity) : ComprovanteAPI{
        return ComprovanteAPI(
            id = entity.id,
            entregaId = entity.entregaId,
            momento = entity.momento,
            latitude = entity.latitude,
            longitude = entity.longitude,
            recebedor = entity.recebedor,
            imgComprovante = entity.imgComprovante
        )
    }
}
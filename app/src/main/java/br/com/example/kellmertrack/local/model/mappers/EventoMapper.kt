package br.com.example.kellmertrack.local.model.mappers

import br.com.example.kellmertrack.core.Sistema
import br.com.example.kellmertrack.local.model.DTO.EventoDTO
import br.com.example.kellmertrack.local.model.entities.EventoEntity

class EventoMapper {

    fun fromEventoEntityToEventoDTO(evento : EventoEntity): EventoDTO {
        return EventoDTO(
            id = evento.id,
            entregaId = evento.entregaId,
            contrato = evento.contrato,
            momento = evento.momento,
            latitude = evento.latitude,
            longitude = evento.longitude,
            veiculo = Sistema.getSetup()?.veiculosId,
            tipo = evento.tipo,
            texto = evento.texto
        )
    }
}
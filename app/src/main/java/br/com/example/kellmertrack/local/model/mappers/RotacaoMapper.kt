package br.com.example.kellmertrack.local.model.mappers

import br.com.example.kellmertrack.local.model.DTO.RotacaoDTO
import br.com.example.kellmertrack.local.model.entities.RotacaoEntity

class RotacaoMapper {

    fun fromRotacaoEntityToDTO(rotacao: RotacaoEntity) : RotacaoDTO {
        return RotacaoDTO(
            id = rotacao.id,
            dispositivo = rotacao.dispositivo,
            veiculo = rotacao.veiculoId,
            rpm = rotacao.rpm,
            momento = rotacao.momento,
            entregaId = rotacao.entregaId,
            bateria = rotacao.bateria,
            temperatura = rotacao.temperatura,
            direcao = rotacao.direcao,
        )
    }
}
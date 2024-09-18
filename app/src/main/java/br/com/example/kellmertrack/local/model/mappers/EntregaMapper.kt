package br.com.example.kellmertrack.local.model.mappers

import br.com.example.kellmertrack.local.model.DTO.ClienteDTO
import br.com.example.kellmertrack.local.model.DTO.ContratoDTO
import br.com.example.kellmertrack.local.model.DTO.EntregaAPI
import br.com.example.kellmertrack.local.model.DTO.ObraDTO
import br.com.example.kellmertrack.local.model.entities.ClienteEntity
import br.com.example.kellmertrack.local.model.entities.EntregaEntity
import br.com.example.kellmertrack.local.model.entities.ObraEntity
import br.com.example.kellmertrack.local.model.entities.relation.ContratoWithObraAndCliente

class EntregaMapper {

    fun toEntregaEntity(entrega : EntregaAPI) : EntregaEntity {
        return EntregaEntity(
            id = entrega.id,
            momento = entrega.momento,
            veiculo = entrega.veiculo,
            contrato = entrega.contrato.numero,
            status = entrega.status,
            quantidade = entrega.quantidade,
            quantidadeEntregue = 0.0,
            dataEntradaObra = entrega.dataEntradaObra,
            dataEntradaUsina = entrega.dataEntradaUsina,
            dataSaidaObra = entrega.dataSaidaObra,
            dataSaidaUsina = entrega.dataSaidaUsina
        )
    }

    
}

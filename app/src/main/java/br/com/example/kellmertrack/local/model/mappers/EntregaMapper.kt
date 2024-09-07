package br.com.example.kellmertrack.local.model.mappers

import br.com.example.kellmertrack.local.model.DTO.ClienteDTO
import br.com.example.kellmertrack.local.model.DTO.ContratoDTO
import br.com.example.kellmertrack.local.model.DTO.EntregaAPI
import br.com.example.kellmertrack.local.model.DTO.ObraDTO
import br.com.example.kellmertrack.local.model.entities.ClienteEntity
import br.com.example.kellmertrack.local.model.entities.EntregaEntity
import br.com.example.kellmertrack.local.model.entities.ObraEntity
import br.com.example.kellmertrack.local.model.entities.relation.ContratoWithObraAndCliente
import br.com.example.kellmertrack.local.model.entities.relation.EntregaWithObra

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

    fun toEntregaDTO(entrega : EntregaWithObra?): EntregaAPI {
        return EntregaAPI(
            id = entrega?.entregaEntity?.id!!,
            momento = entrega.entregaEntity.momento,
            veiculo = entrega.entregaEntity.veiculo,
            contrato = criaContratoDTO(entrega.contratoEntity),
            status = entrega.entregaEntity.status,
            quantidade = entrega.entregaEntity.quantidade,
            dataEntradaObra = entrega.entregaEntity.dataEntradaObra,
            dataEntradaUsina = entrega.entregaEntity.dataEntradaUsina,
            dataSaidaObra = entrega.entregaEntity.dataSaidaObra,
            dataSaidaUsina = entrega.entregaEntity.dataSaidaUsina
        )
    }

    fun criaContratoDTO(contrato : ContratoWithObraAndCliente) : ContratoDTO {
        return ContratoDTO(
            numero = contrato.contratoEntity.numero,
            empresa = contrato.contratoEntity.empresa,
            cliente = criaClienteDTO(contrato.clienteEntity),
            obra = criaObraDTO(contrato.obraEntity)
        )
    }

    fun criaObraDTO(obra : ObraEntity) : ObraDTO{
        return ObraDTO(
            id = obra.id,
            descricao = obra.descricao,
            cidade = obra.cidade,
            bairro = obra.bairro,
            numero = obra.numero.toInt(),
            complemento = "",
            latitude = obra.latitude,
            longitude = obra.longitude,
            raio = 0.0
        )
    }

    fun criaClienteDTO(cliente : ClienteEntity) : ClienteDTO{
        return ClienteDTO(
            id = cliente.id,
            nome = cliente.nome,
            cpf = cliente.cpf,
            cnpj = cliente.cnpj,
            email = cliente.email
        )
    }
}

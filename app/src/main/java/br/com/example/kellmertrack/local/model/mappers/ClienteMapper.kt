package br.com.example.kellmertrack.local.model.mappers

import br.com.example.kellmertrack.local.model.DTO.ClienteDTO
import br.com.example.kellmertrack.local.model.entities.ClienteEntity

class ClienteMapper {

    fun toClienteEntity(cliente: ClienteDTO): ClienteEntity {
        return ClienteEntity(
            id = cliente.id,
            nome = cliente.nome,
            cpf = cliente.cpf,
            cnpj = cliente.cnpj,
            email = cliente.email
        )
    }
}
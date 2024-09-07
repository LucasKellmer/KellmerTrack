package br.com.example.kellmertrack.local.repository

import br.com.example.kellmertrack.local.dao.ClienteDao
import br.com.example.kellmertrack.local.model.entities.ClienteEntity
import javax.inject.Inject

class ClienteRepository @Inject constructor(
    private val clienteDao: ClienteDao
) {
    suspend fun salvar(cliente: ClienteEntity) {
        clienteDao.insert(cliente)
    }

}
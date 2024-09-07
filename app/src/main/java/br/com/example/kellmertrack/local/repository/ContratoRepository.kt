package br.com.example.kellmertrack.local.repository

import br.com.example.kellmertrack.local.dao.ContratoDao
import br.com.example.kellmertrack.local.model.entities.ContratoEntity
import javax.inject.Inject

class ContratoRepository @Inject constructor(
    private val contratoDao: ContratoDao
){
    suspend fun salvar(contrato: ContratoEntity){
        contratoDao.insert(contrato)
    }
}
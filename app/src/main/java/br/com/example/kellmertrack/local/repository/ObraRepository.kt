package br.com.example.kellmertrack.local.repository

import br.com.example.kellmertrack.local.dao.ObraDao
import br.com.example.kellmertrack.local.model.entities.ObraEntity
import javax.inject.Inject

class ObraRepository @Inject constructor(
    private val obraDao: ObraDao
){
    suspend fun salvar(obra: ObraEntity){
        obraDao.insert(obra)
    }
}
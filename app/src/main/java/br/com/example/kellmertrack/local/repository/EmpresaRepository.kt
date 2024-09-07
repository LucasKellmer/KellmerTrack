package br.com.example.kellmertrack.local.repository

import br.com.example.kellmertrack.local.dao.EmpresaDao
import br.com.example.kellmertrack.local.model.entities.EmpresaEntity
import javax.inject.Inject

class EmpresaRepository @Inject constructor(
    private val empresaDao : EmpresaDao
) {

    fun getEmpresa() : EmpresaEntity {
        return empresaDao.getEmpresa()
    }
}
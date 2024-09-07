package br.com.example.kellmertrack.local.model.mappers

import br.com.example.kellmertrack.local.model.DTO.EmpresaDTO
import br.com.example.kellmertrack.local.model.entities.EmpresaEntity

class EmpresaMapper {

    fun toEmpresaEntity(empresa : EmpresaDTO) : EmpresaEntity {
        return EmpresaEntity(
            codigo = empresa.codigo,
            nome = empresa.nome,
            latitude = empresa.latitude,
            longitude = empresa.longitude,
            raio = empresa.raio
        )
    }
}
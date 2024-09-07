package br.com.example.kellmertrack.local.model.mappers

import br.com.example.kellmertrack.local.model.DTO.DispositivoDTO
import br.com.example.kellmertrack.local.model.entities.SetupEntity

class SetupMapper {
    fun fromSetupDTOtoEntity(dispositivo : DispositivoDTO) : SetupEntity {
        return SetupEntity(
            numeroInterno = dispositivo.numeroInterno,
            veiculosId = dispositivo.veiculo,
            mac = dispositivo.mac,
            empresa = dispositivo.empresa.codigo,
            modelo = dispositivo.modelo
        )
    }
}
package br.com.example.kellmertrack.local.repository

import br.com.example.kellmertrack.local.dao.SetupDao
import br.com.example.kellmertrack.local.model.DTO.DispositivoDTO
import br.com.example.kellmertrack.remote.service.CommonService
import javax.inject.Inject

class ConfigRepository @Inject constructor(
    private var commonService : CommonService,
    private var setupDao : SetupDao,
) {

    suspend fun buscaAtualizacaoCadastro(numeroInterno : String?) : DispositivoDTO? {
        return try {
            val response = commonService.buscarDispositivo(numeroInterno)
            if (response.isSuccessful){
                response.body()
            }else{
                null
            }
        }catch ( e :Exception){
            println("Erro: ${e.message}")
            null
        }
    }

    suspend fun atualizaMacDispositivo(mac : String, modelo : String) {
        setupDao.atualizaMacDispositivo(mac, modelo)
    }
}
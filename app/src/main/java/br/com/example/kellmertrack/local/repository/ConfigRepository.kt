package br.com.example.kellmertrack.local.repository

import android.util.Log
import br.com.example.kellmertrack.TAG
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
            Log.d(TAG, "buscaAtualizacaoCadastro response: $response")
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

    suspend fun atualizaMacDispositivo(mac : String) {
        setupDao.atualizaMacDispositivo(mac)
    }

    /*suspend fun buscaEntrega(veiculo : String): RemoteResponse<List<EntregaAPI>?> {
        return try {
            val response = commonService.buscaEntrega(veiculo)
            println(response)
            if (response.body() != null) {
                val entregaResponse = response.body()
                RemoteResponse.success(entregaResponse)
            } else
                RemoteResponse.error(response.code().toString(), null)
        }catch (e: java.lang.Exception) {
            RemoteResponse.error("Entrega não encontrada", null)
        }
    }*/
}
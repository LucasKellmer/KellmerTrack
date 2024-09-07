package br.com.example.kellmertrack.local.repository

import android.util.Log
import br.com.example.kellmertrack.TAG
import br.com.example.kellmertrack.local.dao.EmpresaDao
import br.com.example.kellmertrack.local.dao.SetupDao
import br.com.example.kellmertrack.local.model.DTO.DispositivoDTO
import br.com.example.kellmertrack.remote.model.RemoteResponse
import br.com.example.kellmertrack.local.model.entities.SetupEntity
import br.com.example.kellmertrack.local.model.mappers.EmpresaMapper
import br.com.example.kellmertrack.remote.service.CommonService
import javax.inject.Inject

class SetupRepository @Inject constructor(
    private val setupDao: SetupDao,
    private val empresaDao: EmpresaDao,
    private val commonService: CommonService,
        ){

    fun buscaSetupLocalLive() = setupDao.getSetupLive()

    fun buscaSetup() = setupDao.getSetup()

    suspend fun buscaSetup(dispositivo: String?): RemoteResponse<DispositivoDTO> {
        return try {
            Log.d(TAG, "============= dispositivoParametro $dispositivo ")
            if (dispositivo != null) {
                val response = commonService.buscarDispositivo(dispositivo)
                Log.d(TAG, "=============== response body: ${response.body()} ")
                if (response.body() != null) {
                    val dispositivoResponse = response.body()
                    empresaDao.insert(EmpresaMapper().toEmpresaEntity(response.body()!!.empresa))
                    RemoteResponse.success(dispositivoResponse)
                } else
                    RemoteResponse.error(response.code().toString(), null)
            } else
                RemoteResponse.error("Informe o código do seu dispositivo!", null)
        }catch (e: java.lang.Exception) {
            RemoteResponse.error("Dispositivo não encontrado", null)
        }
    }

    suspend fun salvaDispositivo(dispositivo: SetupEntity){
        commonService.atualizaDataVinculo(dispositivo.numeroInterno)
        return setupDao.insert(dispositivo)
    }
}
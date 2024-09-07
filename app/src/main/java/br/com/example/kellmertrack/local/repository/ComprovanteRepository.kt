package br.com.example.kellmertrack.local.repository

import android.util.Log
import br.com.example.kellmertrack.local.dao.ComprovanteDao
import br.com.example.kellmertrack.local.model.DTO.ComprovanteAPI
import br.com.example.kellmertrack.local.model.DTO.ComprovanteDTO
import br.com.example.kellmertrack.local.model.entities.ComprovanteEntity
import br.com.example.kellmertrack.local.model.mappers.ComprovanteMapper
import br.com.example.kellmertrack.remote.model.RemoteResponse
import br.com.example.kellmertrack.remote.service.CommonService
import br.com.example.kellmertrack.services.tasks.JobCreator
import br.com.example.kellmertrack.ui.utils.FileAdapter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Collections
import javax.inject.Inject

class ComprovanteRepository @Inject constructor(
    private val commonService: CommonService,
    private val comprovanteDao : ComprovanteDao,
    private val entregaRepository: EntregaRepository,
    private val fileAdapter: FileAdapter

){

    fun findComprovanteByEntregaId(entregaId : Int) : ComprovanteDTO{
        val comprovante = comprovanteDao.findComprovanteByEntregaId(entregaId)
        val entrega = entregaRepository.findEntregaById(entregaId.toString())
        return ComprovanteDTO(
            id = comprovante?.id,
            contrato = entrega?.entregaEntity?.contrato,
            entregaId = entregaId,
            cliente = entrega?.contratoEntity?.clienteEntity?.nome,
            obra = entrega?.contratoEntity?.obraEntity?.descricao,
            quantidade = (if(entrega?.entregaEntity?.quantidadeEntregue != 0.0) entrega?.entregaEntity?.quantidadeEntregue else entrega.entregaEntity.quantidade).toString(),
            recebedor = comprovante?.recebedor,
            assinatura = comprovante?.assinatura,
            uriComprovante = comprovante?.uriComprovante,
            imgComprovante = null
        )
    }

    suspend fun salvarComprovante(comprovanteDTO : ComprovanteDTO){
        val comprovanteEntity = ComprovanteMapper().fromDtoToEntity(comprovanteDTO)
        comprovanteDao.insert(comprovanteEntity)
        comprovanteSync()
        //comprovanteEntity.imgComprovante = comprovanteDTO.imgComprovante
        //sincronizaComprovante(comprovanteEntity, comprovanteDTO.imgComprovante)
    }

    suspend fun comprovanteSync(): RemoteResponse<List<ComprovanteAPI>>{
        return try {
            val comprovantes = getComprovantesSync()
            comprovantes.forEach {
                Log.d("ComprovanteSync", it.toString())
            }
            val response = commonService.salvarComprovantes(comprovantes)

            if(response.isSuccessful){
                for (comprovante in comprovantes){
                    comprovanteDao.updateComprovanteSincronizado(comprovante.entregaId, true)
                }
                RemoteResponse.success(comprovantes)
            } else
                RemoteResponse.error(response.message(), null)
        }catch ( e: Exception){
            Log.e("ComprovanteSync", "Não foi possível sincronizar os comprovantes")
            RemoteResponse.error(e.message!!, null)
        }
    }

    private fun getComprovantesSync() : List<ComprovanteAPI>{
        val comprovantes = comprovanteDao.getComprovanteSync()
        val comprovantesApi = mutableListOf<ComprovanteAPI>()
        comprovantes.forEach { comprovante ->
            val fileAndName = comprovante.uriComprovante?.let { FileAdapter.FileAndName(it, "${comprovante.id}.png") }
            val imgFile = fileAndName?.let { fileAdapter.getFileByUri(it) }
            if(imgFile != null){
                val imageZipped = fileAdapter.zipFiles(Collections.singletonList(imgFile))
                comprovante.imgComprovante = imageZipped
            }
            comprovantesApi.add(ComprovanteMapper().fromEntityToApi(comprovante))
        }
        return comprovantesApi
    }

    suspend fun updateComprovanteSincronizado(entregaId : Int) {
        comprovanteDao.updateComprovanteSincronizado(entregaId, true)
    }

    private fun createComprovantePart(imageBytes: ByteArray): MultipartBody.Part {
        val requestBody = imageBytes.toRequestBody("application/zip".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("comprovante", "image.zip", requestBody)
    }
}
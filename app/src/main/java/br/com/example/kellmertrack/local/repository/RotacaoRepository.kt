package br.com.example.kellmertrack.local.repository

import android.util.Log
import br.com.example.kellmertrack.TAG
import br.com.example.kellmertrack.local.dao.RotacaoDao
import br.com.example.kellmertrack.local.model.entities.RotacaoEntity
import br.com.example.kellmertrack.local.model.mappers.RotacaoMapper
import br.com.example.kellmertrack.remote.service.FirebaseService
import javax.inject.Inject

class RotacaoRepository @Inject constructor(
    private val rotacaoDao: RotacaoDao,
    private val firebaseService: FirebaseService
){

    suspend fun salvaDadosRotacao(rotacao : RotacaoEntity){
        val rotacaoDTO = RotacaoMapper().fromRotacaoEntityToDTO(rotacao)
        Log.d(TAG, "================= rotacao prestes a ser salva: ")
        Log.d(TAG, "$rotacao")
        rotacaoDao.insert(rotacao)
        if(!rotacao.sincronizado){
            if (firebaseService.enviaRotacoesFirebase(rotacaoDTO))
                rotacaoDao.updateRotacaoSincronizado(rotacao.id, true)
        }
    }

    suspend fun deleteRotacao(){
        rotacaoDao.deleteAll()
    }

    suspend fun enviaRotacaoFirebase(){
        rotacaoDao.getSincronizar().forEach {rotacao ->
            val rotacaoDTO = RotacaoMapper().fromRotacaoEntityToDTO(rotacao)
            if(firebaseService.enviaRotacoesFirebase(rotacaoDTO))
                rotacaoDao.updateRotacaoSincronizado(rotacao.id, true)
        }
    }

    suspend fun buscaUltimoRotacao() : RotacaoEntity?{
        return rotacaoDao.buscaUltimoRotacao()
    }
}
package br.com.example.kellmertrack.local.repository

import br.com.example.kellmertrack.local.dao.TrajetoDao
import br.com.example.kellmertrack.local.model.entities.TrajetoEntity
import br.com.example.kellmertrack.local.model.mappers.TrajetoMapper
import br.com.example.kellmertrack.remote.service.FirebaseService
import javax.inject.Inject

class TrajetoRepository @Inject constructor(
    private val trajetoDao: TrajetoDao,
    //private val firebaseService: FirebaseService
){

    suspend fun salvaDadosTrajeto(trajeto : TrajetoEntity){
        val trajetoDTO = TrajetoMapper().fromTrajetoEntityToDTO(trajeto)
        trajetoDao.insert(trajeto)
        if(!trajeto.sincronizado){
            //if (firebaseService.enviaLocalizacaoFirebase(trajetoDTO))
            //    trajetoDao.updateTrajetoSincronizado(trajeto.id)
        }
    }

    suspend fun deleteTrajeto(){
        trajetoDao.deleteAll()
    }

    suspend fun enviaTrajetoFirebase(){
        trajetoDao.getSincronizar()?.forEach {trajeto ->
            val trajetoDTO = TrajetoMapper().fromTrajetoEntityToDTO(trajeto)
            //if(firebaseService.enviaLocalizacaoFirebase(trajetoDTO))
            //    trajetoDao.updateTrajetoSincronizado(trajeto.id)
        }
    }

    fun updateTrajetoSincronizado(trajetoId : String){
        trajetoDao.updateTrajetoSincronizado(trajetoId)
    }
}
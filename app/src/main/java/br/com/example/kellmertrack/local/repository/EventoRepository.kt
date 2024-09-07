package br.com.example.kellmertrack.local.repository

import androidx.lifecycle.LiveData
import br.com.example.kellmertrack.local.dao.EventoDao
import br.com.example.kellmertrack.local.model.entities.EventoEntity
import br.com.example.kellmertrack.local.model.mappers.EventoMapper
import br.com.example.kellmertrack.remote.service.FirebaseService
import javax.inject.Inject

class EventoRepository @Inject constructor(
    private val eventoDao : EventoDao,
    private val firebaseService : FirebaseService
) {

    suspend fun salvarEvento(evento : EventoEntity){
        eventoDao.insert(evento)
        val eventoDTO = EventoMapper().fromEventoEntityToEventoDTO(evento)
        if(!evento.sincronizado){
            if (firebaseService.enviaEventoFirebase(eventoDTO))
                evento.id?.let { eventoDao.updateEventoSincronizado(it) }
        }
    }

    fun getEventos() : LiveData<List<EventoEntity>> = eventoDao.eventosLiveData()

    suspend fun enviaEventosFirebase(){
        eventoDao.getSincronizar()?.forEach { evento ->
            val eventoDTO = EventoMapper().fromEventoEntityToEventoDTO(evento)
            if(firebaseService.enviaEventoFirebase(eventoDTO))
                eventoDao.updateEventoSincronizado(evento.id)
        }
    }

    suspend fun buscaUltimoEvento(entregaId : String) : EventoEntity?{
        return eventoDao.buscaUltimoEvento(entregaId)
    }
}
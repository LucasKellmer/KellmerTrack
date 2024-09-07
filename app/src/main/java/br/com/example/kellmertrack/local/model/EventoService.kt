package br.com.grupohobi.kellmertrack.local.model

import android.content.Context
import android.util.Log
import br.com.example.kellmertrack.CHEGADA_OBRA
import br.com.example.kellmertrack.CHEGADA_USINA
import br.com.example.kellmertrack.SAIDA_OBRA
import br.com.example.kellmertrack.SAIDA_USINA
import br.com.example.kellmertrack.TAG
import br.com.example.kellmertrack.TAG_TASK_DESCARREGAMENTO
import br.com.example.kellmertrack.local.location.GeofenceTransition
import br.com.example.kellmertrack.local.model.TipoEvento
import br.com.example.kellmertrack.local.model.entities.EntregaEntity
import br.com.example.kellmertrack.local.model.entities.EventoEntity
import br.com.example.kellmertrack.local.model.entities.relation.EntregaWithObra
import br.com.example.kellmertrack.local.repository.EntregaRepository
import br.com.example.kellmertrack.local.repository.EventoRepository
import br.com.example.kellmertrack.services.tasks.TaskCreator
import br.com.grupohobi.kellmertrack.services.tasks.TaskDescarregamento
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.Date
import javax.inject.Inject

class EventoService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val eventoRepository: EventoRepository,
    private val entregaRepository: EntregaRepository,
    private val taskCreator: TaskCreator
) {
    suspend fun novaTransicaoAsync(transition: GeofenceTransition) = coroutineScope {
        async {
            val entrega = entregaRepository.findEntregaById(transition.entregaId)
            val entregaAtiva = entregaRepository.findEntregaAtiva()
            Log.d("geofence", "novaTransicaoAsync: $transition")
            Log.d(TAG, "entregaAtiva != null: ${entregaAtiva != null}, entregaAtiva.status == 2: ${entregaAtiva?.status == 2}, transition.entregaId == 0: ${transition.entregaId}")

            if (verificaEvento(transition)){
                createEvento(transition.entregaId, transition.tipo, transition.location, entrega, entregaAtiva)
            }
            if(entregaAtiva != null && entregaAtiva.status == 2 && transition.entregaId == "0" && transition.tipo == TipoEvento.ENTRADA){
                iniciaDescarregamento()
            }
            return@async transition
        }
    }

    private suspend fun verificaEvento(transition: GeofenceTransition): Boolean{
        val ultimoEvento = eventoRepository.buscaUltimoEvento(transition.entregaId)
        Log.d(TAG, "verificaEvento: ultimoEvento: ${ultimoEvento?.tipo}, transition.tipo.tipo: ${transition.tipo.tipo}")
        return if (ultimoEvento == null || ultimoEvento.tipo != transition.tipo.tipo){
            true
        }else{
            false
        }
    }

    private fun iniciaDescarregamento() {
        taskCreator.uniqueRequest(TaskDescarregamento::class, TAG_TASK_DESCARREGAMENTO, emptyMap(),  cancelAll = true)
    }

    private suspend fun createEvento(entregaId : String, tipo : TipoEvento, location : LatLng, entrega : EntregaWithObra?, entregaAtiva : EntregaEntity?){

        val evento = EventoEntity.createBasicEvent(entregaId, entregaAtiva?.contrato ,tipo)

        evento.apply {
            latitude = location.latitude
            longitude = location.longitude
            momento = Date()
        }

        if(entregaId == "0"){
            val texto = when(tipo){
                TipoEvento.ENTRADA -> "Chegada na usina"
                TipoEvento.SAIDA -> "Saída da usina"
                else -> ""
            }
            when(tipo){
                TipoEvento.ENTRADA -> entregaRepository.atualizaMomentoChegadaSaida(Date(),entregaAtiva?.id.toString(), CHEGADA_USINA)
                TipoEvento.SAIDA -> entregaRepository.atualizaMomentoChegadaSaida(Date(),entregaAtiva?.id.toString(), SAIDA_USINA)
                else -> ""
            }
            evento.texto = texto
        }else{
            evento.texto = when(tipo){
                TipoEvento.ENTRADA -> "ENTROU na área da obra - ${entrega?.contratoEntity?.obraEntity?.descricao ?: ""}"
                TipoEvento.SAIDA -> "SAIU da área da obra - ${entrega?.contratoEntity?.obraEntity?.descricao ?: ""}"
                TipoEvento.PERMANECEU -> "PERMANECEU na área da obra - ${entrega?.contratoEntity?.obraEntity?.descricao ?: ""}"
                else -> ""
            }
            when(tipo){
                TipoEvento.ENTRADA -> entregaRepository.atualizaMomentoChegadaSaida(Date(), entregaAtiva?.id.toString(),
                    CHEGADA_OBRA
                )
                TipoEvento.SAIDA -> entregaRepository.atualizaMomentoChegadaSaida(Date(),entregaAtiva?.id.toString(), SAIDA_OBRA)
                else -> ""
            }
        }
        eventoRepository.salvarEvento(evento)
    }
}
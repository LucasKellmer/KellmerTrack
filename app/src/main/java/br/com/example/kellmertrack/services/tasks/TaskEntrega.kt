package br.com.example.kellmertrack.services.tasks

import android.util.Log
import br.com.example.kellmertrack.TAG_TASK_ENTREGA
import br.com.example.kellmertrack.local.model.DTO.EntregaAPI
import br.com.example.kellmertrack.local.model.entities.EventoEntity
import br.com.example.kellmertrack.remote.model.Status
import br.com.example.kellmertrack.local.model.TipoEvento
import br.com.example.kellmertrack.local.repository.EntregaRepository
import br.com.example.kellmertrack.local.repository.EventoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class TaskEntrega @Inject constructor(
    private val entregaRepository: EntregaRepository,
    private val eventoRepository: EventoRepository,
) : TaskKellmertrack {
    override suspend fun runTaskAsync(params: Map<String, Any>) = coroutineScope {
        async {
            val entregaId = params["entregaId"].toString()
            try {
                Log.d(TAG_TASK_ENTREGA, "Task buscando entrega API")
                val response = entregaRepository.buscaEntregaAPI(entregaId)
                if(response.data != null && response .status == Status.SUCCESS){
                    val entrega = response.data
                    //Log.d(TAG_TASK_ENTREGA, "Entrega encontrada: ${entrega}")
                    Log.d(TAG_TASK_ENTREGA, "Limpando entrega atual")
                    entregaRepository.limpaEntregas()
                    Log.d(TAG_TASK_ENTREGA, "Task salvando entrega localmente")
                    entregaRepository.salvarEntrega(response)
                    criaEventoNovaEntrega(entrega)
                    confirmaEntregaRecebida(entrega.id)
                }

                return@async TaskResult.SUCCESS
            }catch (e: Exception){
                Log.d(TAG_TASK_ENTREGA, "Erro ao buscar entrega: ${e.message}: ")
                return@async TaskResult.FAILURE
            }
        }
    }

    private suspend fun criaEventoNovaEntrega(entrega: EntregaAPI){
        val evento = EventoEntity.createBasicEvent(entregaId = entrega.id.toString(), contrato = entrega.contrato.numero, tipo = TipoEvento.NOVA_ENTREGA)
        evento.texto = "Nova entrega recebida, com destino para ${entrega.contrato.obra.descricao}"
        eventoRepository.salvarEvento(evento)
    }

    private suspend fun confirmaEntregaRecebida(entregaId : Int){
        entregaRepository.confirmaEntregaRecebida(entregaId)
    }
}
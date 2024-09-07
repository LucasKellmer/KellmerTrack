package br.com.grupohobi.kellmertrack.services.tasks

import android.content.Context
import android.util.Log
import br.com.example.kellmertrack.TAG_TASK_ENTREGA
import br.com.example.kellmertrack.TAG_TASK_SINC
import br.com.example.kellmertrack.remote.model.Status
import br.com.example.kellmertrack.local.repository.EntregaRepository
import br.com.example.kellmertrack.services.tasks.TaskCreator
import br.com.example.kellmertrack.services.tasks.TaskFirebase
import br.com.example.kellmertrack.services.tasks.TaskKellmertrack
import br.com.example.kellmertrack.services.tasks.TaskResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class TaskDescarregamento @Inject constructor(
    @ApplicationContext private val context: Context,
    private val entregaRepository: EntregaRepository,
    private val taskCreator: TaskCreator
) : TaskKellmertrack {
    override suspend fun runTaskAsync(params: Map<String, Any>) = coroutineScope {
        async {
            try {
                val entrega = entregaRepository.getSoEntregas()
                async { syncAll() }.await()
                    Log.d("descarregamento", "descarregamento entrega: $entrega")
                    entregaRepository.atualizaEntregaStatus(entrega?.entregaEntity?.id!!, 3)
                    val response = entregaRepository.decarregamento(entrega.entregaEntity.id.toString())
                    Log.d("descarregamento", "descarregamento response: $response")
                    if(response .status == Status.SUCCESS){
                        Log.d(TAG_TASK_ENTREGA, "Limpando entrega atual")
                        val descarregamento = entregaRepository.limpaEntregaCompleto()
                        if (descarregamento)
                            return@async TaskResult.SUCCESS
                        else
                            return@async TaskResult.RETRY
                    }
                return@async TaskResult.RETRY
            }catch (e: Exception){
                Log.d(TAG_TASK_ENTREGA, "Erro ao realizar descarregamento: ${e.message}: ")
                return@async TaskResult.FAILURE
            }
        }
    }

    private fun syncAll() {
        taskCreator.uniqueRequest(TaskFirebase::class, TAG_TASK_SINC, emptyMap())
    }
}
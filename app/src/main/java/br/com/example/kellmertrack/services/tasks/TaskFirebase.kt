package br.com.example.kellmertrack.services.tasks

import android.util.Log
import br.com.example.kellmertrack.TAG_TASK_ENTREGA
import br.com.example.kellmertrack.TAG_TASK_EVENTOS
import br.com.example.kellmertrack.TAG_TASK_FIREBASE
import br.com.example.kellmertrack.local.repository.EntregaRepository
import br.com.example.kellmertrack.local.repository.EventoRepository
import br.com.example.kellmertrack.local.repository.RotacaoRepository
import br.com.example.kellmertrack.local.repository.TrajetoRepository
import br.com.example.kellmertrack.remote.service.FirebaseService
import br.com.example.kellmertrack.ui.utils.DispositivoFunctions
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class TaskFirebase @Inject constructor(
    private val rotacaoRepository: RotacaoRepository,
    private val trajetoRepository: TrajetoRepository,
    private val entregaRepository: EntregaRepository,
    private val eventoRepository: EventoRepository,
    private val dispositivoFunctions: DispositivoFunctions,
    private val firebaseService: FirebaseService
) : TaskKellmertrack {

    override suspend fun runTaskAsync(params: Map<String, Any>)= coroutineScope {
        async {
            try {
                Log.d(TAG_TASK_FIREBASE, "Task Sincronizando sensor firebase")
                rotacaoRepository.enviaRotacaoFirebase()
                Log.d(TAG_TASK_FIREBASE, "Task Sincronizando localizacao firebase")
                trajetoRepository.enviaTrajetoFirebase()
                Log.d(TAG_TASK_ENTREGA, "Task Sincronizando entregas firebase")
                entregaRepository.enviaEntregaFirebase()
                Log.d(TAG_TASK_EVENTOS, "Task Sincronizando eventos firebase")
                eventoRepository.enviaEventosFirebase()
                dispositivoFunctions.criaDispositivoStatus()?.let{
                    firebaseService.criaDispostivoStatus(it)
                }
                return@async TaskResult.SUCCESS
            } catch (e: Exception) {
                return@async TaskResult.FAILURE
            }
        }
    }
}
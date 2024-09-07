package br.com.example.kellmertrack.services.tasks

import br.com.example.kellmertrack.local.repository.RotacaoRepository
import br.com.example.kellmertrack.local.repository.TrajetoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class
TaskDBKellmertrack @Inject constructor(
    private val trajetoRepository: TrajetoRepository,
    private val rotacaoRepository: RotacaoRepository
) : TaskKellmertrack {

    override suspend fun runTaskAsync(params: Map<String, Any>) = coroutineScope {
        async {
            try {
                rotacaoRepository.deleteRotacao()
                trajetoRepository.deleteTrajeto()
                return@async TaskResult.SUCCESS
            } catch (e: Exception) {
                return@async TaskResult.FAILURE
            }
        }
    }
}
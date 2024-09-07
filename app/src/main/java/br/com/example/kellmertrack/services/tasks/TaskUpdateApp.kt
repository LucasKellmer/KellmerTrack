package br.com.example.kellmertrack.services.tasks

import br.com.example.kellmertrack.local.model.entities.HobitrackVersionEntity
import br.com.example.kellmertrack.local.repository.HobitrackVersionRepository
import br.com.example.kellmertrack.remote.service.FirebaseService
import br.com.example.kellmertrack.services.tasks.TaskKellmertrack
import br.com.example.kellmertrack.services.tasks.TaskResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class TaskUpdateApp @Inject constructor(
    private val hobitrackVersionRepository: HobitrackVersionRepository,
    private val firebaseService: FirebaseService
): TaskKellmertrack {

    override suspend fun runTaskAsync(params: Map<String, Any>) = coroutineScope{
        async {
            try {
                val lambda = { hobitrackVersionEntity : HobitrackVersionEntity ->
                    hobitrackVersionRepository.salvaTrackVersion(hobitrackVersionEntity)
                }
                firebaseService.buscaAtuApp(lambda)
                return@async TaskResult.SUCCESS
            } catch (e: Exception) {
                return@async TaskResult.FAILURE
            }
        }
    }
}
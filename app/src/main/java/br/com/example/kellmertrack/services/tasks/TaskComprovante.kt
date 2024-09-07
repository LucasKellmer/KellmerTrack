package br.com.example.kellmertrack.services.tasks

import br.com.example.kellmertrack.local.repository.ComprovanteRepository
import br.com.example.kellmertrack.remote.service.CommonService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class TaskComprovante @Inject constructor(
    private val comprovanteRepository: ComprovanteRepository
) : TaskKellmertrack {

    override suspend fun runTaskAsync(params: Map<String, Any>) = coroutineScope {
        async {
            try {
                sincronizaComprovantes()
                return@async TaskResult.SUCCESS
            } catch (e: Exception) {
                return@async TaskResult.FAILURE
            }
        }
    }

    suspend fun sincronizaComprovantes() = coroutineScope {
        comprovanteRepository.comprovanteSync()
    }
}
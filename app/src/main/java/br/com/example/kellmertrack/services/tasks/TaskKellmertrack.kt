package br.com.example.kellmertrack.services.tasks

import kotlinx.coroutines.Deferred

interface TaskKellmertrack {
    suspend fun runTaskAsync(params:Map<String, Any>): Deferred<TaskResult>
}
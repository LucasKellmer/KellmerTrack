package br.com.example.kellmertrack.services.tasks

import br.com.grupohobi.kellmertrack.services.tasks.TaskDescarregamento
import javax.inject.Inject

class JobCreator @Inject constructor(
    private val taskFirebase: TaskFirebase,
    private val taskDbTesteSensor: TaskDBKellmertrack,
    private val taskUpdateApp: TaskUpdateApp,
    private val taskEntrega: TaskEntrega,
    private val taskDescarregamento: TaskDescarregamento,
    private val taskComprovante : TaskComprovante,
) {

    fun createTaskTesteSensor(type: String): TaskKellmertrack {
        return when (type) {
            TaskFirebase::class.simpleName -> taskFirebase
            TaskDBKellmertrack::class.simpleName -> taskDbTesteSensor
            TaskUpdateApp::class.simpleName -> taskUpdateApp
            TaskEntrega::class.simpleName -> taskEntrega
            TaskDescarregamento::class.simpleName -> taskDescarregamento
            TaskComprovante::class.simpleName -> taskComprovante
            else -> throw RuntimeException("Task with type $type not found")
        }
    }
}

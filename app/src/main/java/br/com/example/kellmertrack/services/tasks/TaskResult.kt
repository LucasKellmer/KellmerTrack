package br.com.example.kellmertrack.services.tasks

enum class TaskResult(val taskResult: String) {
    SUCCESS("SUCCESS"),
    FAILURE("FAILURE"),
    RETRY("RETRY")
}
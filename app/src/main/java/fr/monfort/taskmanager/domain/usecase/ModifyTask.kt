package fr.monfort.taskmanager.domain.usecase

import fr.monfort.taskmanager.data.repository.TaskRepository

class ModifyTask (
    private val repository: TaskRepository
) {
    fun execute(id: String, title: String) {
        val trim = title.trim()
        if (trim.isBlank()) return
        repository.updateTitle(id, trim)
    }
}
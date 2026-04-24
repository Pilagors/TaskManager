package fr.monfort.taskmanager.domain.usecase

import fr.monfort.taskmanager.data.model.Task
import fr.monfort.taskmanager.data.repository.TaskRepository
import java.util.UUID

class CreateTask (
    private val repository: TaskRepository
) {
    fun execute (title: String, parentId: String? = null) {
        repository.addTask(
            Task(
                id = UUID.randomUUID().toString(),
                title = title,
                parentId = parentId,
                order = 0
            )
        )
    }
}
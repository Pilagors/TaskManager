package fr.monfort.taskmanager.domain.usecase

import fr.monfort.taskmanager.data.model.Task
import fr.monfort.taskmanager.data.repository.TaskRepository
import java.util.UUID

class CreateTask (
    private val repository: TaskRepository
) {
    fun execute (title: String, parentId: String? = null) : String {
        val id : String = UUID.randomUUID().toString()
        repository.addTask(
            Task(
                id = id,
                title = title,
                parentId = parentId,
                order = 0
            )
        )
        return id
    }
}
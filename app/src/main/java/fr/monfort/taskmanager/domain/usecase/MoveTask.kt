package fr.monfort.taskmanager.domain.usecase

import fr.monfort.taskmanager.data.repository.TaskRepository

class MoveTask(
    val repository: TaskRepository
) {
    fun execute(id: String, newParentId: String?, order: Int) {
        repository.moveTask(
            taskId = id,
            newParentId = newParentId,
            newOrder = order
        )
    }
}
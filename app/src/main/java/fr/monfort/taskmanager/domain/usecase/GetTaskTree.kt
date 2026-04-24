package fr.monfort.taskmanager.domain.usecase

import fr.monfort.taskmanager.data.repository.TaskRepository
import fr.monfort.taskmanager.data.model.Task

class GetTaskTree(
    private val repo: TaskRepository
) {
    fun execute(parentId: String? = null): List<Task> {
        return repo.getChildren(parentId)
    }
}
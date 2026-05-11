package fr.monfort.taskmanager.domain.usecase

import fr.monfort.taskmanager.data.repository.TaskRepository

class DeleteTask (
    private val repository: TaskRepository
) {
    fun execute (taskId: String) {

        val children = repository.getChildren(taskId)
        children.forEach { child ->
            execute(child.id)
        }

        repository.deleteTask(taskId)
    }
}
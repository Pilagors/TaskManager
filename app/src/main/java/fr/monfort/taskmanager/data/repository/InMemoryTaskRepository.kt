package fr.monfort.taskmanager.data.repository

import fr.monfort.taskmanager.data.model.Task

class InMemoryTaskRepository : TaskRepository {
    private val tasks = mutableListOf<Task>()

    override fun getAllTasks(): List<Task> {
        return tasks
    }

    override fun getChildren(parentId: String?): List<Task> {
        return tasks.filter { it.parentId == parentId }
                    .sortedBy { it.order }
    }

    override fun getTaskById(id: String): Task? {
        return tasks.find { it.id == id }
    }

    override fun addTask(task: Task) {
        tasks.add(task)
    }

    override fun deleteTask(taskId: String) {
        tasks.removeAll { it.id == taskId}
    }

    override fun updateTask(task: Task) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) tasks[index] = task
    }

    override fun moveTask(taskId: String, newParentId: String?, newOrder: Int) {
        val task = getTaskById(taskId) ?: return

        updateTask(
            task.copy(
                parentId = newParentId,
                order = newOrder
            )
        )
    }
}
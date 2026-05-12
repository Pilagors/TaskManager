package fr.monfort.taskmanager.data.repository

import fr.monfort.taskmanager.data.model.Task
import kotlinx.coroutines.flow.StateFlow

interface TaskRepository {
    val tasks: StateFlow<List<Task>>

    fun getTaskById(id: String): Task?

    fun getChildren(parentId: String?): List<Task>

    fun addTask(task: Task)

    fun updateTask(task: Task)

    fun updateTitle(taskId: String, newTitle: String)

    fun deleteTask(taskId: String)

    fun moveTask(taskId: String, newParentId: String?, newOrder: Int)
}
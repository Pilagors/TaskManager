package fr.monfort.taskmanager.data.repository

import fr.monfort.taskmanager.data.model.Task

interface TaskRepository {
    fun getAllTasks(): List<Task>

    fun getTaskById(id: String): Task?

    fun getChildren(parentId: String?): List<Task>

    fun addTask(task: Task)

    fun updateTask(task: Task)

    fun deleteTask(taskId: String)

    fun moveTask(taskId: String, newParentId: String?, newOrder: Int)
}
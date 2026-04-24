package fr.monfort.taskmanager.data.model

data class TaskNode(
    val task: Task,
    val children: List<TaskNode>
)

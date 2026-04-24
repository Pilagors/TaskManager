package fr.monfort.taskmanager.data.model

data class Task (
    val id: String,
    val title: String,
    val parentId: String? = null,
    val order: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
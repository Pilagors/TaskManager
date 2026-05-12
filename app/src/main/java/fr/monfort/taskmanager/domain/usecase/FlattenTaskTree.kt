package fr.monfort.taskmanager.domain.usecase

import fr.monfort.taskmanager.data.model.TaskNode

data class FlatTask(
    val id: String,
    val title: String,
    val parentId: String?,
    val level: Int,
    val hasChildren: Boolean
)

class FlattenTaskTree {
    fun execute(nodes: List<TaskNode>, level: Int = 0): List<FlatTask> = buildList {
        for (node in nodes) {
            add(
                FlatTask(
                    id = node.task.id,
                    title = node.task.title,
                    parentId = node.task.parentId,
                    level = level,
                    hasChildren = node.children.isNotEmpty()
                )
            )

            if (node.children.isNotEmpty()) {
                addAll(execute(node.children, level + 1))
            }
        }
    }
}
package fr.monfort.taskmanager.domain.usecase

import fr.monfort.taskmanager.data.model.TaskNode

data class FlatTask(
    val id: String,
    val title: String,
    val parentId: String?,
    val level: Int,
    val hasChildren: Boolean,
    val order: Int
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
                    hasChildren = node.children.isNotEmpty(),
                    node.task.order
                )
            )

            if (node.children.isNotEmpty()) {
                addAll(execute(node.children, level + 1))
            }
        }
    }
}

fun List<FlatTask>.descendantIdsOf(id: String): Set<String> {
    val index = indexOfFirst { it.id == id }

    if (index == -1) return emptySet()

    val baseLevel = this[index].level
    val result = mutableSetOf<String>()
    var i = index + 1

    while (i < size && this[i].level > baseLevel) {
        result.add(this[i].id)
        i++
    }

    return result
}
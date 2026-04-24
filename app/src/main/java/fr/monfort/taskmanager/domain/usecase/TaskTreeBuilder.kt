package fr.monfort.taskmanager.domain.usecase

import fr.monfort.taskmanager.data.model.Task
import fr.monfort.taskmanager.data.model.TaskNode

class TaskTreeBuilder {
    fun build(tasks: List<Task>, parentId: String? = null): List<TaskNode> {
        return tasks.filter { it.parentId == parentId }
                    .sortedBy { it.order }
                    .map { task ->
                        TaskNode(
                            task = task,
                            children = build(tasks, task.id)
                        )
                    }
    }
}
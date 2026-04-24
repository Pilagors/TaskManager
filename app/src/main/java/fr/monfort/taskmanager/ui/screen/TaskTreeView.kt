package fr.monfort.taskmanager.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import fr.monfort.taskmanager.data.model.TaskNode

@Composable
fun TaskTreeView(
    nodes: List<TaskNode>,
    level: Int
) {
    Column() {
        nodes.forEach { node ->
            TaskRow(
                task = node.task,
                level = level
            )

            if (node.children.isNotEmpty()) {
                TaskTreeView(
                    nodes = node.children,
                    level = level + 1
                )
            }
        }
    }
}
package fr.monfort.taskmanager.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.monfort.taskmanager.data.model.TaskNode

@Composable
fun TaskTreeView(
    nodes: List<TaskNode>,
    level: Int
) {
    Column() {
        nodes.forEach { node ->
            Card(
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier
                .padding(start = (level * 32).dp, top = 4.dp, bottom = 4.dp)
            ) {
                TaskRow(task = node.task)

                if (node.children.isNotEmpty()) {
                    TaskTreeView(
                        nodes = node.children,
                        level = level + 1
                    )
                }
            }
        }
    }
}
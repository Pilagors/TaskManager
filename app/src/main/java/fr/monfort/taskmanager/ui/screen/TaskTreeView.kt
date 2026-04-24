package fr.monfort.taskmanager.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                elevation = CardDefaults.cardElevation(2.dp),
                border = if (level == 0) {
                    BorderStroke(1.dp, Color.Gray)
                } else {
                    null
                },
                modifier = Modifier
                .padding(
                    start = if (level != 0) (level * 32).dp else 16.dp,
                    top = 4.dp,
                    bottom = if (level == 0) 16.dp else 4.dp,
                    end = 16.dp
                )
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
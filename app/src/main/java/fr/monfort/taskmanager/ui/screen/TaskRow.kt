package fr.monfort.taskmanager.ui.screen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.monfort.taskmanager.data.model.Task

@Composable
fun TaskRow(
    task: Task,
    level: Int
) {
    Row(
        modifier = Modifier
            .padding(start = (level * 16).dp, top = 4.dp, bottom = 4.dp)
    ) {
        Text(task.title)
    }
}
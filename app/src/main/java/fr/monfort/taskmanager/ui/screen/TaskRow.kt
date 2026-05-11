package fr.monfort.taskmanager.ui.screen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.monfort.taskmanager.data.model.Task
import fr.monfort.taskmanager.data.repository.TaskRepository
import fr.monfort.taskmanager.domain.usecase.DeleteTask

@Composable
fun TaskRow(
    task: Task,
    repository: TaskRepository
) {
    Row(

    ) {
        Text(
            text = task.title,
            modifier = Modifier
                .padding(12.dp)
                .weight(1f)
        )

        IconButton(onClick = {
            DeleteTask(repository).execute(taskId = task.id)
        }) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Fermer"
            )
        }
    }
}
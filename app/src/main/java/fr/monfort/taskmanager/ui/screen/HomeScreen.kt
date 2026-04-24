package fr.monfort.taskmanager.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import fr.monfort.taskmanager.data.repository.TaskRepository
import fr.monfort.taskmanager.domain.usecase.TaskTreeBuilder

@Composable
fun HomeScreen(repository: TaskRepository) {
    val tasks = remember { repository.getAllTasks() }

    val tree = remember(tasks) {
        TaskTreeBuilder().build(tasks, null)
    }

    TaskTreeView(nodes = tree, level = 0)
}
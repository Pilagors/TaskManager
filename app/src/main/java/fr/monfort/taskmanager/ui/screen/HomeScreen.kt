package fr.monfort.taskmanager.ui.screen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.monfort.taskmanager.data.repository.TaskRepository
import fr.monfort.taskmanager.domain.usecase.FlattenTaskTree
import fr.monfort.taskmanager.domain.usecase.TaskTreeBuilder

@Composable
fun HomeScreen(repository: TaskRepository) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val treeBuilder = remember { TaskTreeBuilder() }
    val flattener = remember { FlattenTaskTree() }
    val flatList = remember(tasks) {
        flattener.execute(treeBuilder.build(tasks, null))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { focusManager.clearFocus() }
                )
            }
    ) {
        LazyColumn(
           contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(flatList, key = { it.id }) { flatTask ->
                TaskRowFlat(
                    flatTask = flatTask,
                    onTitleChange = { viewModel.modifyTitle(flatTask.id, it) },
                    onDelete = { viewModel.deleteTask(flatTask.id) }
                )
            }
        }
    }
}
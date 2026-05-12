package fr.monfort.taskmanager.ui.screen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.monfort.taskmanager.data.repository.TaskRepository
import fr.monfort.taskmanager.domain.usecase.FlatTask
import fr.monfort.taskmanager.domain.usecase.FlattenTaskTree
import fr.monfort.taskmanager.domain.usecase.TaskTreeBuilder
import fr.monfort.taskmanager.domain.usecase.descendantIdsOf
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun HomeScreen(repository: TaskRepository) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    val treeBuilder = remember { TaskTreeBuilder() }
    val flattener = remember { FlattenTaskTree() }
    val flatList = remember(tasks) {
        flattener.execute(treeBuilder.build(tasks, null))
    }

    var draggingId by remember { mutableStateOf<String?>(null) }

    val displayedList = remember(flatList, draggingId) {
        val id = draggingId ?: return@remember flatList
        val dragged = flatList.firstOrNull() { it.id == id } ?: return@remember flatList
        flatList.filter { it.parentId == dragged.parentId }
    }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState = lazyListState) { from, to ->
        val newSiblingOrder = displayedList.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }

        applySiblingReorderToRepo(newSiblingOrder, vm = viewModel)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
            state = lazyListState,
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(displayedList, key = { it.id }) { flatTask ->
                ReorderableItem(reorderState, key = flatTask.id) { isDragging ->
                    TaskRowFlat(
                        flatTask = flatTask,
                        isDragging = isDragging,
                        isHoveredAsParent = false,
                        onTitleChange = { viewModel.modifyTitle(flatTask.id, it) },
                        onDelete = { viewModel.deleteTask(flatTask.id) },
                        dragHandleModifier = Modifier.draggableHandle(
                            onDragStarted = {
                                draggingId = flatTask.id
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDragStopped = {
                                draggingId = null
                            }
                        )
                    )
                }
            }
        }
    }
}

private fun applySiblingReorderToRepo(
    newSiblingOrder: List<FlatTask>,
    vm: HomeViewModel,
) {
    newSiblingOrder.forEachIndexed { index, item ->
        vm.moveTask(item.id, item.parentId, index)
    }
}
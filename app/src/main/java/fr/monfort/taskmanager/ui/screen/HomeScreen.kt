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

    var hoveredParentId by remember { mutableStateOf<String?>(null) }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState = lazyListState) { from, to ->
        val draggedId = flatList[from.index].id
        val targetIndex = to.index

        val newFlat = flatList.toMutableList().apply {
            val item = removeAt(from.index)
            add(targetIndex, item)
        }

        applyFlatOrderToRepo(newFlat, viewModel)
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
            items(flatList, key = { it.id }) { flatTask ->
                ReorderableItem(reorderState, key = flatTask.id) { isDragging ->
                    TaskRowFlat(
                        flatTask = flatTask,
                        isDragging = isDragging,
                        isHoveredAsParent = hoveredParentId == flatTask.id,
                        onTitleChange = { viewModel.modifyTitle(flatTask.id, it) },
                        onDelete = { viewModel.deleteTask(flatTask.id) },
                        dragHandleModifier = Modifier.draggableHandle(
                            onDragStarted = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDragStopped = {
                                hoveredParentId?.let { parentId ->
                                    if (parentId != flatTask.id) {
                                        viewModel.moveTask(flatTask.id, parentId, 0)
                                    }
                                }
                                hoveredParentId = null
                            }
                        )
                    )
                }
            }
        }
    }
}

private fun applyFlatOrderToRepo(flatList: List<FlatTask>, vm: HomeViewModel) {
    val orderByParent = mutableMapOf<String?, Int>()
    flatList.forEach { item ->
        val current = orderByParent.getOrDefault(item.parentId, 0)
        vm.moveTask(item.id, item.parentId, current)
        orderByParent[item.parentId] = current + 1
    }
}

package fr.monfort.taskmanager.ui.screen

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.monfort.taskmanager.data.repository.TaskRepository
import fr.monfort.taskmanager.domain.usecase.FlatTask
import fr.monfort.taskmanager.domain.usecase.FlattenTaskTree
import fr.monfort.taskmanager.domain.usecase.TaskTreeBuilder
import kotlinx.coroutines.launch

enum class DropZone { Before, Inside, After }

data class DropTarget(
    val taskId: String,
    val zone: DropZone,
)

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

    val dragState = remember { DragState() }
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(dragState.isDragging, dragState.pointerY) {
        if (dragState.isDragging) {
            val edge = 100f
            val viewportTop = 0f
            val viewportBottom = lazyListState.layoutInfo.viewportSize.height.toFloat()
            val y = dragState.pointerY
            val scrollSpeed = when {
                y < viewportTop + edge -> -8f
                y > viewportBottom - edge -> 8f
                else -> 0f
            }
            if (scrollSpeed != 0f) {
                scope.launch { lazyListState.scrollBy(scrollSpeed) }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(flatList, key = { it.id }) { flatTask ->
                TaskRowFlat(
                    flatTask = flatTask,
                    dragState = dragState,
                    allTasks = flatList,
                    onTitleChange = { viewModel.modifyTitle(flatTask.id, it) },
                    onDelete = { viewModel.deleteTask(flatTask.id) },
                    onDragStart = {
                        dragState.start(flatTask.id)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDragMove = { pointerPos ->
                        dragState.updatePointer(pointerPos)
                    },
                    onDragEnd = {
                        val target = dragState.currentTarget
                        val draggedId = dragState.draggingId
                        if (target != null && draggedId != null) {
                            applyDrop(
                                draggedId = draggedId,
                                target = target,
                                flatList = flatList,
                                vm = viewModel,
                            )
                        }
                        dragState.stop()
                    },
                )
            }
        }
    }
}

class DragState {
    var draggingId by mutableStateOf<String?>(null)
        private set
    var pointerY by mutableStateOf(0f)
        private set
    var pointerX by mutableStateOf(0f)
        private set
    var dragOffsetY by mutableStateOf(0f)
    var currentTarget by mutableStateOf<DropTarget?>(null)
        private set
    data class ItemInfo(val bounds: Rect, val hasChildren: Boolean)
    val itemBounds = mutableStateMapOf<String, ItemInfo>()

    val isDragging: Boolean get() = draggingId != null

    fun start(id: String) {
        draggingId = id
        currentTarget = null
        dragOffsetY = 0f
    }

    fun updatePointer(absolutePos: Offset) {
        pointerY = absolutePos.y
        pointerX = absolutePos.x
        recomputeTarget()
    }

    fun stop() {
        draggingId = null
        currentTarget = null
        dragOffsetY = 0f
    }

    private fun recomputeTarget() {
        val draggedId = draggingId ?: return
        val hit = itemBounds.entries.firstOrNull { (id, info) ->
            id != draggedId && pointerY in info.bounds.top..info.bounds.bottom
        } ?: run {
            currentTarget = null
            return
        }
        val (targetId, info) = hit
        val relativeY = (pointerY - info.bounds.top) / info.bounds.height
        val zone = if (info.hasChildren) {
            if (relativeY < 0.25f) DropZone.Before else DropZone.Inside
        } else {
            when {
                relativeY < 0.25f -> DropZone.Before
                relativeY > 0.75f -> DropZone.After
                else -> DropZone.Inside
            }
        }
        currentTarget = DropTarget(targetId, zone)
    }
}
@Composable
fun TaskRowFlat(
    flatTask: FlatTask,
    dragState: DragState,
    allTasks: List<FlatTask>,
    onTitleChange: (String) -> Unit,
    onDelete: () -> Unit,
    onDragStart: () -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit,
) {
    var isEditing by remember { mutableStateOf(false) }
    var draftTitle by remember(flatTask.id) { mutableStateOf(flatTask.title) }
    val focusRequester = remember { FocusRequester() }
    var hasBeenFocused by remember { mutableStateOf(false) }
    var rowTopInWindow by remember { mutableStateOf(0f) }

    val isThisDragging = dragState.draggingId == flatTask.id
    val target = dragState.currentTarget
    val showBeforeIndicator = target?.taskId == flatTask.id && target.zone == DropZone.Before
    val showAfterIndicator = target?.taskId == flatTask.id && target.zone == DropZone.After
    val showInsideHighlight = target?.taskId == flatTask.id && target.zone == DropZone.Inside

    Box(
        modifier = Modifier
            .onGloballyPositioned { coords ->
                val bounds = coords.boundsInWindow()
                dragState.itemBounds[flatTask.id] = DragState.ItemInfo(bounds, flatTask.hasChildren)
                rowTopInWindow = bounds.top
            }
            .graphicsLayer {
                if (isThisDragging) {
                    translationY = dragState.dragOffsetY
                    alpha = 0.7f
                }
            }
    ) {
        if (showBeforeIndicator) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.TopCenter)
            )
        }

        Card(
            elevation = CardDefaults.cardElevation(if (isThisDragging) 8.dp else 2.dp),
            border = if (showInsideHighlight) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else if (flatTask.level == 0) {
                BorderStroke(1.dp, Color.Gray)
            } else null,
            modifier = Modifier.padding(
                start = (16 + flatTask.level * 32).dp,
                top = 4.dp,
                bottom = 4.dp,
                end = 16.dp,
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isEditing) {
                    Text(
                        text = flatTask.title,
                        modifier = Modifier
                            .padding(12.dp)
                            .weight(1f)
                            .pointerInput(Unit) {
                                detectTapGestures(onDoubleTap = {
                                    draftTitle = flatTask.title
                                    isEditing = true
                                })
                            }
                    )
                } else {
                    TextField(
                        value = draftTitle,
                        onValueChange = { draftTitle = it },
                        singleLine = true,
                        modifier = Modifier
                            .padding(12.dp)
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .onFocusChanged { state ->
                                if (state.isFocused) hasBeenFocused = true
                                else if (hasBeenFocused) {
                                    onTitleChange(draftTitle)
                                    isEditing = false
                                    hasBeenFocused = false
                                }
                            },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onTitleChange(draftTitle)
                            isEditing = false
                        })
                    )
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }
                }

                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Déplacer",
                    modifier = Modifier
                        .padding(8.dp)
                        .pointerInput(flatTask.id) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    onDragStart()
                                    onDragMove(Offset(offset.x, rowTopInWindow + offset.y))
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragState.dragOffsetY += dragAmount.y
                                    val absoluteY = rowTopInWindow + change.position.y + dragState.dragOffsetY
                                    onDragMove(Offset(change.position.x, absoluteY))
                                },
                                onDragEnd = { onDragEnd() },
                                onDragCancel = { onDragEnd() },
                            )
                        },
                )

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Close, contentDescription = "Supprimer")
                }
            }
        }

        if (showAfterIndicator) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

private fun applyDrop(
    draggedId: String,
    target: DropTarget,
    flatList: List<FlatTask>,
    vm: HomeViewModel,
) {
    if (draggedId == target.taskId) return

    if (isDescendantOf(target.taskId, draggedId, flatList)) return

    val targetTask = flatList.firstOrNull { it.id == target.taskId } ?: return

    val newParentId: String? = when (target.zone) {
        DropZone.Inside -> target.taskId
        DropZone.Before, DropZone.After -> targetTask.parentId
    }

    val destSiblings = flatList
        .filter { it.parentId == newParentId && it.id != draggedId }
        .toMutableList()

    val insertIndex = when (target.zone) {
        DropZone.Inside -> 0
        DropZone.Before -> destSiblings.indexOfFirst { it.id == target.taskId }
            .coerceAtLeast(0)
        DropZone.After -> destSiblings.indexOfFirst { it.id == target.taskId }
            .let { if (it == -1) destSiblings.size else it + 1 }
    }

    destSiblings.forEachIndexed { idx, sib ->
        val newOrder = if (idx < insertIndex) idx else idx + 1
        if (sib.parentId != newParentId || sib.order != newOrder) {
            vm.moveTask(sib.id, newParentId, newOrder)
        }
    }
    vm.moveTask(draggedId, newParentId, insertIndex)
}
private fun isDescendantOf(
    candidateId: String,
    ancestorId: String,
    flatList: List<FlatTask>,
): Boolean {
    var currentId: String? = candidateId
    while (currentId != null) {
        if (currentId == ancestorId && currentId != candidateId) return true
        val parent = flatList.firstOrNull { it.id == currentId }?.parentId
        if (parent == ancestorId) return true
        currentId = parent
    }
    return false
}
package fr.monfort.taskmanager.ui.screen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import fr.monfort.taskmanager.data.model.Task
import fr.monfort.taskmanager.data.repository.TaskRepository
import fr.monfort.taskmanager.domain.usecase.DeleteTask
import fr.monfort.taskmanager.domain.usecase.ModifyTask

@Composable
fun TaskRow(
    task: Task,
    repository: TaskRepository
) {
    var isEditing by remember { mutableStateOf(false) }
    var draftTitle by remember(task.id) { mutableStateOf(task.title) }
    val focusRequester = remember { FocusRequester() }
    var hasBeenFocused by remember { mutableStateOf(false) }

    Row(

    ) {
        if (!isEditing) {
            Text(
                text = task.title,
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectTapGestures (
                            onDoubleTap = {
                                draftTitle = task.title
                                isEditing = true
                            }
                        )
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
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            hasBeenFocused = true
                        } else if (hasBeenFocused) {
                            ModifyTask(repository).execute(task.id, draftTitle)
                            isEditing = false
                            hasBeenFocused = false
                        }
                    },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    ModifyTask(repository).execute(task.id, draftTitle)
                    isEditing = false
                })
            )

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }

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
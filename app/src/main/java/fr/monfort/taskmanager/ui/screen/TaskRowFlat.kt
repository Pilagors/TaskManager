package fr.monfort.taskmanager.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import fr.monfort.taskmanager.domain.usecase.FlatTask

@Composable
fun TaskRowFlat(
    flatTask: FlatTask,
    onTitleChange: (String) -> Unit,
    onDelete: () -> Unit,
) {
    var isEditing by remember { mutableStateOf(false) }
    var draftTitle by remember(flatTask.id) { mutableStateOf(flatTask.title) }
    val focusRequester = remember { FocusRequester() }
    var hasBeenFocused by remember { mutableStateOf(false) }

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        border = if (flatTask.level == 0) BorderStroke(1.dp, Color.Gray) else null,
        modifier = Modifier.padding(
            start = (16 + flatTask.level * 32).dp,
            top = 4.dp,
            bottom = 4.dp,
            end = 16.dp,
        )
    ) {
        Row {
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
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, contentDescription = "Supprimer")
            }
        }
    }
}
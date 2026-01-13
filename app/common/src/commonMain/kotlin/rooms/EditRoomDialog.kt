package io.ktor.chat.rooms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.ktor.chat.*
import io.ktor.chat.ui.components.ErrorText
import io.ktor.chat.ui.theme.Radius
import io.ktor.chat.ui.theme.Space
import io.ktor.chat.utils.tryRequest

@Composable
fun EditRoomDialog(
    room: Room,
    onEdit: suspend (Room) -> Unit,
    onClose: () -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue(room.name)) }
    val loading = remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun editRoom() {
        coroutineScope.tryRequest(loading, { error = it }) {
            onEdit(room.copy(name = name.text))
            onClose()
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(Radius.xs)),
                tonalElevation = 24.dp,
                shadowElevation = 16.dp,
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.padding(Space.md)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Space.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Edit Room",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline,
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(Space.md))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Room Name") },
                        placeholder = { Text("Enter room name") },
                        singleLine = true,
                        isError = error != null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.outline,
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.outline,
                            focusedPlaceholderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(Radius.xs)
                    )

                    error?.let {
                        Spacer(modifier = Modifier.height(Space.sm))
                        ErrorText(it)
                    }

                    Spacer(modifier = Modifier.height(Space.md))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onClose,
                            modifier = Modifier.padding(end = Space.sm)
                        ) {
                            Text(
                                "Cancel",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = ::editRoom,
                            enabled = name.text.isNotBlank() && !loading.value,
                            shape = RoundedCornerShape(Radius.xs),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(if (loading.value) "Saving..." else "Save")
                        }
                    }
                }
            }
        }
    }
}
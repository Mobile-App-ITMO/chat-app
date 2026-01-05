package io.ktor.chat.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.ktor.chat.utils.insert
import io.ktor.chat.utils.tryRequest

@Composable
fun MessageInput(
    modifier: Modifier = Modifier,
    send: suspend (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var message by remember { mutableStateOf(TextFieldValue("")) }
    val loading = remember { mutableStateOf(false) }

    fun sendMessage(): Boolean {
        if (message.text.isBlank()) return true
        val messageToSend = message.text.trim().also {
            message = TextFieldValue("")
        }
        coroutineScope.tryRequest(loading) {
            send(messageToSend.trim())
        }
        return true
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(
                color = MaterialTheme.colorScheme.surface
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.extraSmall
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    BasicTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterStart)
                            .onPreviewKeyEvent { e ->
                                if (e.key != Key.Enter) false
                                else {
                                    if (!e.isShiftPressed) {
                                        sendMessage()
                                    } else if (e.type == KeyEventType.KeyDown) {
                                        message = message.insert("\n")
                                    }
                                    true
                                }
                            },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp)
                                    .align(Alignment.CenterStart)
                            ) {
                                if (message.text.isEmpty()) {
                                    Text(
                                        text = "Type a message...",
                                        modifier = Modifier.align(Alignment.CenterStart),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = { sendMessage() }
                        )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { sendMessage() },
                    modifier = Modifier.size(36.dp),
                    enabled = message.text.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send message",
                        tint = if (message.text.isNotBlank()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        }
                    )
                }
            }
        }
    }
}
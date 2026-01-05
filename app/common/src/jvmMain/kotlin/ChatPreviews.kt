package io.ktor.chat

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import io.ktor.chat.ui.screens.chat.ChatScreen
import io.ktor.chat.vm.createViewModel

@Preview
@Composable
fun ChatScreenPreview() {
    ChatScreen(createViewModel())
}
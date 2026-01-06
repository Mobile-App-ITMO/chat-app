package io.ktor.chat.messages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.chat.Message
import io.ktor.chat.emoml.EmotionOutput
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messages: SnapshotStateList<Message>,
    emotions: Map<Long, EmotionOutput> = emptyMap()
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages) {
        snapshotFlow { messages.size }
            .distinctUntilChanged()
            .filter { it > 0 }
            .collect { size ->
                listState.animateScrollToItem(index = size - 1)
            }
    }

    when (messages.size) {
        0 -> Box(modifier = modifier.fillMaxSize()) {
            androidx.compose.material3.Text(
                "Nothing here yet...",
                modifier = Modifier.align(Center).padding(10.dp, 5.dp)
            )
        }
        else -> LazyColumn(modifier, state = listState) {
            items(messages) { message ->
                MessageListItem(message, emotion = emotions[message.id])
            }
        }
    }
}

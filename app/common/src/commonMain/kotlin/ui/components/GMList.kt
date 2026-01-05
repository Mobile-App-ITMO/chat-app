package io.ktor.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.ktor.chat.Membership
import io.ktor.chat.client.remoteListWithUpdates
import io.ktor.chat.utils.Remote
import io.ktor.chat.vm.ChatViewModel

@Composable
fun MessageList(
    vm: ChatViewModel,
    onRoomSelected: (Membership) -> Unit = {}
) {
    val currentUser by vm.loggedInUser

    val roomsRemote: Remote<SnapshotStateList<Membership>> by vm.memberships.remoteListWithUpdates(
        predicate = { it.user.id == currentUser?.id }
    )

    RemoteLoader(roomsRemote) { rooms ->
        if (rooms.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No messages yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(rooms) { membership ->
                    RoomItem(
                        membership = membership,
                        onClick = { onRoomSelected(membership) }
                    )
                }
            }
        }
    }
}

@Composable
fun RoomItem(
    membership: Membership,
    onClick: (Membership) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(membership) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (membership.room.name.isNotEmpty()) {
                Text(
                    text = membership.room.name.first().uppercaseChar().toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = membership.room.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            // - 最后一条消息
            // - 最后发言时间
            // - 未读消息数
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(start = 72.dp, end = 16.dp),
        thickness = 2.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )
}
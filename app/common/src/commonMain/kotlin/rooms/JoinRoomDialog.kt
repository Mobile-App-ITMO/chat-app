package io.ktor.chat.rooms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import io.ktor.chat.*
import io.ktor.chat.ui.components.RemoteLoader
import io.ktor.chat.utils.Remote
import io.ktor.chat.ui.theme.Radius
import io.ktor.chat.ui.theme.Space

@Composable
fun JoinRoomDialog(
    joinedRooms: List<Membership>,
    searchRooms: @Composable (String) -> State<Remote<List<Room>>>,
    onJoin: suspend (Room) -> Unit,
    onClose: () -> Unit
) {
    val search by remember { mutableStateOf("") }
    val roomsRemote by searchRooms(search)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
                    .clip(RoundedCornerShape(Radius.xs)),
                tonalElevation = 24.dp,
                shadowElevation = 16.dp,
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = Space.md, vertical = Space.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Join a Room",
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

                    RemoteLoader(roomsRemote) { allRooms ->
                        val availableRooms = allRooms.filter { room ->
                            joinedRooms.none { it.room.id == room.id }
                        }

                        if (availableRooms.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(Space.sm)
                                ) {
                                    Text(
                                        text = "No rooms available",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    TextButton(onClick = onClose) {
                                        Text(
                                            "Close",
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background),
                                contentPadding = PaddingValues(Space.md),
                                verticalArrangement = Arrangement.spacedBy(Space.sm)
                            ) {
                                items(availableRooms, key = { it.id }) { room ->
                                    RoomItem(
                                        room = room,
                                        onJoinClick = {
                                            coroutineScope.launch {
                                                try {
                                                    onJoin(room)
                                                    snackbarHostState.showSnackbar(
                                                        "Successfully joined ${room.name}!"
                                                    )
                                                    onClose()
                                                } catch (e: Exception) {
                                                    snackbarHostState.showSnackbar(
                                                        "Failed to join room: ${e.message}",
                                                        withDismissAction = true
                                                    )
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = Space.lg),
                contentAlignment = Alignment.BottomCenter
            ) {
                SnackbarHost(hostState = snackbarHostState)
            }
        }
    }
}

@Composable
private fun RoomItem(
    room: Room,
    onJoinClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(Radius.xs),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = Space.md, vertical = Space.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                RoomIcon(room = room)
                Text(
                    text = room.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onJoinClick,
                modifier = Modifier.padding(start = Space.sm),
                shape = RoundedCornerShape(Radius.xs),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Join")
            }
        }
    }
}

@Composable
private fun RoomIcon(room: Room, modifier: Modifier = Modifier) {
    val initials = room.name.take(1).uppercase()

    Surface(
        modifier = modifier.size(40.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.outline
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initials,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
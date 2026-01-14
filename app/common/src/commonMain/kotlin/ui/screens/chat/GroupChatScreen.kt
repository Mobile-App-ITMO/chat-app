package io.ktor.chat.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.chat.JoinCall
import io.ktor.chat.Membership
import io.ktor.chat.Message
import io.ktor.chat.Room
import io.ktor.chat.client.listInRoom
import io.ktor.chat.client.remoteListWithUpdates
import io.ktor.chat.emoml.EmotionOutput
import io.ktor.chat.emoml.SentimentService
import io.ktor.chat.messages.MessageInput
import io.ktor.chat.messages.MessageList
import io.ktor.chat.rooms.EditRoomDialog
import io.ktor.chat.ui.components.IconDropdownButton
import io.ktor.chat.ui.components.MenuItem
import io.ktor.chat.ui.components.RemoteLoader
import io.ktor.chat.utils.Done
import io.ktor.chat.utils.Remote
import io.ktor.chat.vm.ChatViewModel
import io.ktor.chat.vm.VideoCallViewModel
import io.ktor.chat.vm.createViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import kotlinx.datetime.Clock
import kotlin.collections.ArrayDeque

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    vm: ChatViewModel = createViewModel(),
    videoCallVM: VideoCallViewModel? = null,
    onBack: () -> Unit
) {
    var selectedRoom by remember { vm.room }
    val currentUser by remember { vm.loggedInUser }
    val roomsRemote: Remote<SnapshotStateList<Membership>> by vm.memberships.remoteListWithUpdates(
        predicate = { it.user.id == currentUser!!.id }
    )

    var showEditRoomDialog by remember { mutableStateOf(false) }
    var editingRoom by remember { mutableStateOf<Room?>(null) }

    val messagesRemote: Remote<SnapshotStateList<Message>> by vm.messages.listInRoom(selectedRoom?.room)
    val scope = rememberCoroutineScope()

    var showIncomingCallDialog by remember { mutableStateOf(false) }
    var callingRequest by remember { mutableStateOf<JoinCall?>(null) }

    LaunchedEffect(Unit) {
        videoCallVM?.callRequests?.collect { request ->
            callingRequest = request
            showIncomingCallDialog = true
        }
    }

    fun existingRoomWithId(id: Long): Membership? = when (roomsRemote) {
        is Done -> (roomsRemote as Done<SnapshotStateList<Membership>>).value.firstOrNull { it.room.id == id }
        else -> null
    }

    if (showEditRoomDialog && editingRoom != null) {
        EditRoomDialog(
            room = editingRoom!!,
            onEdit = { updatedRoom ->
                scope.launch {
                    vm.rooms.update(updatedRoom)
                    selectedRoom = selectedRoom?.copy(room = updatedRoom)
                }
            },
            onClose = {
                showEditRoomDialog = false
                editingRoom = null
            }
        )
    }

    if (showIncomingCallDialog && callingRequest != null) {
        AlertDialog(
            onDismissRequest = {
                showIncomingCallDialog = false
                scope.launch { videoCallVM?.rejectCall(callingRequest!!) }
            },
            title = { Text("Incoming Call") },
            text = { Text("${callingRequest!!.sender.name} is calling. Do you want to join?") },
            confirmButton = {
                Button(
                    onClick = {
                        showIncomingCallDialog = false
                        selectedRoom = existingRoomWithId(callingRequest!!.roomId) ?: return@Button
                        scope.launch { videoCallVM?.acceptCall(callingRequest!!) }
                    }
                ) { Text("Accept") }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showIncomingCallDialog = false
                        scope.launch { videoCallVM?.rejectCall(callingRequest!!) }
                    }
                ) { Text("Decline") }
            }
        )
    }

    RemoteLoader(roomsRemote) { rooms ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            ChatTopBar(
                selectedRoom = selectedRoom,
                onBack = {
                    scope.launch { videoCallVM?.reinitSession() }
                    onBack()
                },
                onVideoCallInitiated = {
                    if (selectedRoom != null) {
                        scope.launch { videoCallVM?.initiateCall(selectedRoom!!.room.id) }
                    }
                },
                onLeaveRoom = {
                    selectedRoom?.let { membership ->
                        scope.launch {
                            vm.memberships.delete(membership.id)
                            videoCallVM?.reinitSession()
                            selectedRoom = null
                        }
                    }
                },
                onUpdateRoom = {
                    selectedRoom?.let { membership ->
                        editingRoom = membership.room
                        showEditRoomDialog = true
                    }
                },
                onDeleteRoom = {
                    selectedRoom?.let { membership ->
                        scope.launch {
                            vm.rooms.delete(membership.room.id)
                            videoCallVM?.reinitSession()
                            selectedRoom = null
                        }
                    }
                }
            )

            if (selectedRoom != null) {
                GroupMessagesView(
                    selectedRoom = selectedRoom!!,
                    messagesRemote = messagesRemote,
                    onCreate = { messageText ->
                        scope.launch {
                            vm.messages.create(
                                Message(
                                    author = currentUser!!,
                                    created = Clock.System.now(),
                                    room = selectedRoom!!.room.id,
                                    text = messageText
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    selectedRoom: Membership?,
    onBack: () -> Unit,
    onVideoCallInitiated: () -> Unit,
    onLeaveRoom: () -> Unit,
    onUpdateRoom: () -> Unit,
    onDeleteRoom: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = selectedRoom?.room?.name ?: "Select a Room",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            if (selectedRoom != null) {
                IconDropdownButton(
                    icon = Icons.Default.Settings,
                    iconContentDescription = "Room Settings",
                    iconSize = 24,
                    menuItems = listOf(
                        MenuItem(
                            text = "Leave Room",
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            onClick = onLeaveRoom
                        ),
                        MenuItem(
                            text = "Edit Room",
                            icon = Icons.Default.Edit,
                            onClick = onUpdateRoom
                        ),
                        MenuItem(
                            text = "Delete Room",
                            icon = Icons.Default.Delete,
                            onClick = onDeleteRoom
                        )
                    ),
                    modifier = Modifier.size(32.dp)
                )

                IconButton(onClick = onVideoCallInitiated, enabled = true) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Video Call",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}

@Composable
private fun GroupMessagesView(
    selectedRoom: Membership,
    messagesRemote: Remote<SnapshotStateList<Message>>,
    onCreate: suspend (String) -> Unit
) {
    val sentiment = remember { SentimentService("http://10.0.2.2:11434") }
    val emotions = remember { mutableStateMapOf<Long, EmotionOutput?>() }

    val mutex = remember { Mutex() }
    val queue = remember { ArrayDeque<Message>() }
    var job by remember { mutableStateOf<Job?>(null) }

    val scope = rememberCoroutineScope()

    fun startWorker(messages: List<Message>) {
        if (job?.isActive == true) return
        job = scope.launch {
            while (isActive) {
                val msg = mutex.withLock { if (queue.isEmpty()) null else queue.removeFirst() } ?: break
                val out = runCatching { sentiment.analyze(msg.text) }
                    .getOrElse { EmotionOutput("EMO: error", 0f, EmotionOutput.Tone.NEUTRAL) }
                emotions[msg.id] = out
                yield()
            }
        }
    }

    fun enqueueNewestFirst(messages: List<Message>) {
        val newOnes = messages
            .asSequence()
            .filter { it.id != 0L }
            .filter { emotions[it.id] == null }
            .sortedWith(compareByDescending<Message> { it.created }.thenByDescending { it.id })
            .toList()

        scope.launch {
            mutex.withLock {
                for (m in newOnes) {
                    if (queue.none { it.id == m.id }) queue.addLast(m)
                }
            }
            startWorker(messages)
        }
    }

    RemoteLoader(messagesRemote) { messages ->
        LaunchedEffect(messages.size) {
            enqueueNewestFirst(messages)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            MessageList(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(bottom = 50.dp),
                messages = messages,
                emotions = emotions.filterValues { it != null }.mapValues { it.value!! }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                MessageInput(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(5.dp),
                    send = onCreate
                )
            }
        }
    }
}

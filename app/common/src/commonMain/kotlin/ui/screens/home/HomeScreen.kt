package io.ktor.chat.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.ktor.chat.Membership
import io.ktor.chat.Room
import io.ktor.chat.client.remoteList
import io.ktor.chat.client.remoteListWithUpdates
import io.ktor.chat.ui.components.AddButton
import io.ktor.chat.ui.components.BottomNavBar
import io.ktor.chat.ui.components.MenuItem
import io.ktor.chat.ui.components.MessageList
import io.ktor.chat.ui.components.SettingsList
import io.ktor.chat.vm.ChatViewModel
import io.ktor.chat.rooms.CreateRoomDialog
import io.ktor.chat.rooms.JoinRoomDialog
import io.ktor.chat.ui.components.RemoteLoader
import io.ktor.chat.vm.VideoCallViewModel

sealed class HomeTab(
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    object Home : HomeTab("Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Friends : HomeTab("Friends", Icons.Filled.People, Icons.Outlined.People)
    object Settings : HomeTab("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: ChatViewModel,
    videoCallVM: VideoCallViewModel? = null,
    onRefreshSystemInfo: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf<HomeTab>(HomeTab.Home) }
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var showJoinRoomDialog by remember { mutableStateOf(false) }
    val currentUser by remember { vm.loggedInUser }

    val joinedRoomsRemote by vm.memberships.remoteListWithUpdates(
        predicate = { it.user.id == currentUser!!.id }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Etherlot",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )

            if (selectedTab == HomeTab.Home) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    val menuItems = listOf(
                        MenuItem(
                            text = "Join Group",
                            icon = Icons.Default.GroupAdd,
                            onClick = {
                                showJoinRoomDialog = true
                            }
                        ),
                        MenuItem(
                            text = "Create Group",
                            icon = Icons.Default.Create,
                            onClick = {
                                showCreateRoomDialog = true
                            }
                        )
                    )

                    AddButton(
                        menuItems = menuItems,
                        onDismiss = {
                            println("Menu dismissed")
                        },
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }
        }

        if (showCreateRoomDialog) {
            CreateRoomDialog(
                onCreate = { newRoomName ->
                    vm.rooms.create(Room(newRoomName)).let { newRoom ->
                        vm.room.value = vm.memberships.create(
                            Membership(
                                user = currentUser!!,
                                room = newRoom,
                            )
                        )
                        videoCallVM?.reinitSession()
                    }
                },
                onClose = {
                    showCreateRoomDialog = false
                }
            )
        }

        if (showJoinRoomDialog) {
            RemoteLoader(joinedRoomsRemote) { joinedRooms ->
                JoinRoomDialog(
                    joinedRooms = joinedRooms,
                    searchRooms = { vm.rooms.remoteList() },
                    onJoin = { joinedRoom ->
                        vm.room.value = vm.memberships.create(
                            Membership(
                                user = currentUser!!,
                                room = joinedRoom,
                            )
                        )
                        videoCallVM?.reinitSession()
                    },
                    onClose = {
                        showJoinRoomDialog = false
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                HomeTab.Home -> {
                    MessageList(
                        vm = vm,
                        onRoomSelected = { membership ->
                            vm.room.value = membership
                        }
                    )
                }
                HomeTab.Friends -> {
                    Text(
                        text = "Friends Content",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                HomeTab.Settings -> {
                    SettingsList(
                        vm = vm,
                        onRefreshSystemInfo = onRefreshSystemInfo
                    )
                }
            }
        }

        BottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = { tab -> selectedTab = tab }
        )
    }
}
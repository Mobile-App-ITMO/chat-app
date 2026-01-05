package io.ktor.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.ktor.chat.ui.theme.Space
import io.ktor.chat.vm.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsList(vm: ChatViewModel) {
    val loggedInUser by vm.loggedInUser
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
                    .padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "User Avatar",
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = loggedInUser?.name ?: "Guest",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        SettingCategory(title = "Account") {
            SettingItem(
                title = "Profile",
                icon = Icons.Outlined.Person,
                onClick = { /* 导航到个人资料页面 */ }
            )

            SettingItem(
                title = "Data",
                icon = Icons.Outlined.Lock,
                onClick = { /* 导航到数据设置页面 */ }
            )
        }

        SettingCategory(title = "Preferences") {
            SettingItem(
                title = "Notifications",
                icon = Icons.Outlined.Notifications,
                onClick = { /* 导航到通知设置页面 */ }
            )

            SettingItem(
                title = "Appearance",
                icon = Icons.Outlined.Palette,
                onClick = { /* 导航到外观设置页面 */ }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            TextButton(
                onClick = {
                    scope.launch {
                        vm.logout()
                    }
                },
                modifier = Modifier.align(Alignment.Center),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SettingCategory(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            content()
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SettingItem(
    title: String,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    titleColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = Space.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(Space.lg)
            )

            Spacer(modifier = Modifier.width(Space.md))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(Space.sm))

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(Space.lg)
            )
        }
    }
}
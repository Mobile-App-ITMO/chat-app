package io.ktor.chat.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.ktor.chat.ui.theme.Space

@Composable
fun ToggleButton(
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onIcon: ImageVector = Icons.Filled.Notifications,
    offIcon: ImageVector = Icons.Filled.NotificationsOff,
    iconDescription: String = "info"
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        IconToggleButton(
            isOn = isOn,
            onToggle = onToggle,
            enabled = enabled,
            onIcon = onIcon,
            offIcon = offIcon,
            iconDescription = iconDescription
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun IconToggleButton(
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onIcon: ImageVector = Icons.Filled.Notifications,
    offIcon: ImageVector = Icons.Filled.NotificationsOff,
    iconDescription: String = "info"
) {
    val icon = if (isOn) onIcon else offIcon
    val contentColor = if (isOn) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    IconButton(
        onClick = { onToggle(!isOn) },
        modifier = modifier,
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "$iconDescription ${if (isOn) "on" else "off"}",
            tint = contentColor,
            modifier = Modifier.padding(Space.xs)
        )
    }
}
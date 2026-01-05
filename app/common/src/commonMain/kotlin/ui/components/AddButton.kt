package io.ktor.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.ktor.chat.ui.theme.Radius

data class MenuItem(
    val text: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun IconDropdownButton(
    icon: ImageVector = Icons.Default.Add,
    iconContentDescription: String = "Menu",
    iconSize: Int = 48,
    menuItems: List<MenuItem>,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.size(iconSize.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
                tint = MaterialTheme.colorScheme.primary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                onDismiss()
            },
            shape = RoundedCornerShape(Radius.xs),
            modifier = Modifier
                .width(180.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.extraSmall
                )
                .clip(MaterialTheme.shapes.extraSmall)
        ) {
            menuItems.forEachIndexed { index, menuItem ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = menuItem.icon,
                                contentDescription = menuItem.text,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = menuItem.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        menuItem.onClick()
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun AddButton(
    menuItems: List<MenuItem>,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    IconDropdownButton(
        icon = Icons.Default.Add,
        iconContentDescription = "Add options",
        menuItems = menuItems,
        onDismiss = onDismiss,
        modifier = modifier
    )
}
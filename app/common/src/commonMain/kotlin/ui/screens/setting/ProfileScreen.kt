package io.ktor.chat.ui.screens.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.ktor.chat.ui.components.BackIcon
import io.ktor.chat.ui.theme.Radius
import io.ktor.chat.ui.theme.Space
import io.ktor.chat.utils.LocalStorage
import io.ktor.chat.vm.ChatViewModel

@Composable
fun ProfileScreen(vm: ChatViewModel, onBack: () -> Unit) {
    val loggedInUser by vm.loggedInUser
    val userName = loggedInUser?.name ?: "Guest"
    val localStorage = LocalStorage.getInstance()

    val userEmail by remember { mutableStateOf(localStorage.getEmail()) }

    val userMemoFlow = localStorage.userMemo
    val memoContentFromFlow by userMemoFlow.collectAsState()

    var memoContent by remember { mutableStateOf(memoContentFromFlow) }
    var isInEditMode by remember { mutableStateOf(false) }
    val maxCharCount = 500

    LaunchedEffect(memoContentFromFlow) {
        if (!isInEditMode) {
            memoContent = memoContentFromFlow
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = Space.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            BackIcon(onBack = onBack)
        }

        Spacer(modifier = Modifier.height(Space.xl))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outlineVariant)
                .padding(Space.lg)
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "User Avatar",
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(Space.lg))

        Text(
            text = userName,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(Space.sm))

        Text(
            text = userEmail,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(Space.xl))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Profile Content",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary,
                )
                if (!isInEditMode) {
                    IconButton(
                        onClick = {
                            isInEditMode = true
                            memoContent = memoContentFromFlow
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Space.sm))

            if (isInEditMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    BasicTextField(
                        value = memoContent,
                        onValueChange = {
                            if (it.length <= maxCharCount) {
                                memoContent = it
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Radius.md))
                            .padding(Space.md),
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize
                        ),
                        singleLine = false,
                        maxLines = Int.MAX_VALUE,
                        decorationBox = { innerTextField ->
                            if (memoContent.isEmpty()) {
                                Text(
                                    text = "Write your profile content here...",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            innerTextField()
                        }
                    )
                    Text(
                        text = "${memoContent.length}/$maxCharCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (memoContent.length >= maxCharCount) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = Space.md, vertical = Space.sm)
                    )
                }

                Spacer(modifier = Modifier.height(Space.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isInEditMode = false
                            memoContent = memoContentFromFlow
                        },
                        modifier = Modifier.padding(end = Space.sm),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(Radius.xs)
                    ) {
                        Text(text = "Cancel", fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            localStorage.saveUserMemo(memoContent)
                            isInEditMode = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(Radius.xs)
                    ) {
                        Text(text = "Save", fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                Text(
                    text = memoContentFromFlow.ifEmpty { "No profile content yet." },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = Int.MAX_VALUE
                )
            }
        }
    }
}
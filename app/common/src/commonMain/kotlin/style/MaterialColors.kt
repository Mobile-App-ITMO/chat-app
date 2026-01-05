package io.ktor.chat.style

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

val PrimaryButtonColors: ButtonColors
    @Composable
    get() = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )

val SecondaryButtonColors: ButtonColors
    @Composable
    get() = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary
    )

val OutlineButtonColors: ButtonColors
    @Composable
    get() = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.primary,
        disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
    )

val TextButtonColors: ButtonColors
    @Composable
    get() = ButtonDefaults.textButtonColors(
        contentColor = MaterialTheme.colorScheme.primary,
        disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
    )

val ErrorButtonColors: ButtonColors
    @Composable
    get() = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError,
        disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.38f),
        disabledContentColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.38f)
    )
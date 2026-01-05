package io.ktor.chat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.myapplication.ui.theme.AppTypography

private val BlackWhiteColorScheme = lightColorScheme(
    primary = blackColor,
    onPrimary = whiteColor,

    secondary = whiteColor,
    onSecondary = blackColor,

    background = whiteColor,
    onBackground = blackColor,

    surface = greyColor_3,
    onSurface = blackColor,

    error = redError,
    onError = whiteColor,

    outline = greyColor_1,
    outlineVariant = greyColor_2,

    onSurfaceVariant = card
)

@Composable
fun ChatAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BlackWhiteColorScheme,
        typography = AppTypography,
        content = content
    )
}
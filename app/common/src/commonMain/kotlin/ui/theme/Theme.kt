package io.ktor.chat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.myapplication.ui.theme.AppTypography

private val LightColorScheme = lightColorScheme(
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

private val DarkColorScheme = darkColorScheme(
    primary = darkWhiteColor,
    onPrimary = darkBlackColor,

    secondary = darkBlackColor,
    onSecondary = darkWhiteColor,

    background = darkBlackColor,
    onBackground = darkWhiteColor,

    surface = darkGreyColor_3,
    onSurface = darkWhiteColor,

    error = redError,
    onError = whiteColor,

    outline = darkGreyColor_1,
    outlineVariant = darkGreyColor_2,
    onSurfaceVariant = darkCard
)

@Composable
fun ChatAppTheme(
    content: @Composable () -> Unit
) {
    val systemDarkTheme = isSystemInDarkTheme()
    val shouldUseDarkTheme = if (ThemeManager.useSystemTheme) {
        systemDarkTheme
    } else {
        ThemeManager.isDarkTheme
    }

    val colorScheme = if (shouldUseDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
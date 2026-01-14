package io.ktor.chat.ui.theme

import androidx.compose.runtime.*

object ThemeManager {

    private var _isDarkTheme by mutableStateOf(false)
    private var _useSystemTheme by mutableStateOf(true)

    val isDarkTheme: Boolean
        get() = _isDarkTheme

    val useSystemTheme: Boolean
        get() = _useSystemTheme


    fun toggleTheme() {
        _isDarkTheme = !_isDarkTheme
    }

    fun setDarkTheme(dark: Boolean) {
        _isDarkTheme = dark
    }

    fun toggleSystemTheme() {
        _useSystemTheme = !_useSystemTheme
    }

    fun setSystemTheme(use: Boolean) {
        _useSystemTheme = use
    }
}
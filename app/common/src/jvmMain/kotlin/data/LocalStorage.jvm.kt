package io.ktor.chat.data

import androidx.compose.runtime.Composable

actual object LoginStorage {
    @Composable
    actual fun saveLoginInfo(
        email: String,
        password: String,
        rememberPassword: Boolean
    ) {
    }

    @Composable
    actual fun rememberPasswordState(): Pair<Boolean, (Boolean) -> Unit> {
        TODO("Not yet implemented")
    }

    @Composable
    actual fun useAutoFillLogin(
        onEmailFilled: (String) -> Unit,
        onPasswordFilled: (String) -> Unit
    ) {
    }

    @Composable
    actual fun clearAll() {
    }

    @Composable
    actual fun clearPassword() {
    }
}
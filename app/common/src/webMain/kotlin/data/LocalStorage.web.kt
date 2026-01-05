package io.ktor.chat.data

actual object LoginStorage {
    @androidx.compose.runtime.Composable
    actual fun saveLoginInfo(
        email: String,
        password: String,
        rememberPassword: Boolean
    ) {
    }

    @androidx.compose.runtime.Composable
    actual fun rememberPasswordState(): kotlin.Pair<Boolean, (Boolean) -> Unit> {
        TODO("Not yet implemented")
    }

    @androidx.compose.runtime.Composable
    actual fun useAutoFillLogin(
        onEmailFilled: (String) -> Unit,
        onPasswordFilled: (String) -> Unit
    ) {
    }

    @androidx.compose.runtime.Composable
    actual fun clearAll() {
    }

    @androidx.compose.runtime.Composable
    actual fun clearPassword() {
    }
}
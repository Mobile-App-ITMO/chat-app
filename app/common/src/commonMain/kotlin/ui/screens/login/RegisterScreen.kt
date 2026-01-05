package io.ktor.chat.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import io.ktor.chat.ui.components.BackIcon
import io.ktor.chat.vm.ChatViewModel
import io.ktor.chat.ui.components.ErrorText
import io.ktor.chat.utils.tryRequest

import io.ktor.chat.ui.components.BlackButton
import io.ktor.chat.ui.components.InputField

@Composable
fun RegisterScreen(vm: ChatViewModel, onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var server by remember { vm.server }

    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordRepeat by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val loadingState = remember { mutableStateOf(false) }
    var loading by loadingState

    fun register() {
        if (password != passwordRepeat) {
            error = "Passwords do not match"
            return
        }

        coroutineScope.tryRequest(loadingState, { error = it }) {
            vm.register(server, email, name, password)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary)
    ){
        WelcomeView {
            FormColumn {
                Text(
                    "Create account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )

                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    InputField(
                        value = email,
                        label = "Email",
                        onValueChange = { email = it },
                        placeholder = "Email",
                        keyboardType = KeyboardType.Email,
                        singleLine = true
                    )

                    InputField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Username",
                        placeholder = "Username",
                        singleLine = true
                    )

                    InputField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        placeholder = "Password",
                        isPassword = true,
                        singleLine = true
                    )

                    InputField(
                        value = passwordRepeat,
                        onValueChange = { passwordRepeat = it },
                        label = "Confirm password",
                        placeholder = "Repeat password",
                        isPassword = true,
                        singleLine = true
                    )
                }

                error?.let {
                    ErrorText(it, modifier = Modifier.align(Alignment.End))
                }

                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BlackButton(
                        label = if (loading) "Submitting..." else "Continue",
                        onClick = { register() },
                        enabled = !loading && sequenceOf(
                            server,
                            email,
                            name,
                            password,
                            passwordRepeat
                        ).all { it.isNotBlank() }
                    )
                }
            }
        }

        BackIcon(onBack = onBack)
    }
}
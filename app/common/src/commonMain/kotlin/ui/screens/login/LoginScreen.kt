package io.ktor.chat.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import kotlinx.coroutines.launch
import io.ktor.chat.vm.ChatViewModel
import io.ktor.chat.ui.components.ErrorText
import io.ktor.chat.utils.tryRequest
import io.ktor.chat.utils.LocalStorage

import io.ktor.chat.ui.components.BlackButton
import io.ktor.chat.ui.components.InputField
import io.ktor.chat.ui.components.ToggleButton
import io.ktor.chat.ui.components.WhiteButton
import io.ktor.chat.ui.theme.Space

@Composable
fun LoginScreen(vm: ChatViewModel, onRegisterClick: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var server by remember { vm.server }

    val localStorage = LocalStorage.getInstance()
    val savedEmail by localStorage.email.collectAsState()
    val savedPassword by localStorage.password.collectAsState()
    val savedRemember by localStorage.remember.collectAsState()

    var email by remember { mutableStateOf(savedEmail) }
    var password by remember {
        mutableStateOf(if (savedRemember && savedPassword.isNotBlank()) savedPassword else "")
    }
    var error by remember { mutableStateOf<String?>(null) }
    val loadingState = remember { mutableStateOf(false) }
    var loading by loadingState

    LaunchedEffect(email) {
        if (email.isNotBlank() && email != savedEmail) {
            localStorage.updateEmail(email)
        }
    }

    fun login() {
        coroutineScope.tryRequest(loadingState, { error = it }) {
            vm.login(server, email, password)
            localStorage.saveLogin(
                email = email,
                password = if (savedRemember) password else "",
                remember = savedRemember
            )
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
                    "Login",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(Space.md))

                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    InputField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        placeholder = "Email",
                        keyboardType = KeyboardType.Email,
                        singleLine = true,
                    )

                    InputField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        placeholder = "Password",
                        isPassword = true,
                        singleLine = true
                    )
                }

                error?.let {
                    ErrorText(it, modifier = Modifier.align(Alignment.End))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Space.sm),
                    contentAlignment = Alignment.Center
                ) {
                    ToggleButton(
                        isOn = savedRemember,
                        onToggle = { enabled ->
                            localStorage.saveLogin(
                                email = email,
                                password = if (enabled) password else "",
                                remember = enabled
                            )
                        },
                        label = "Remember password",
                        onIcon = Icons.Filled.CheckCircle,
                        offIcon = Icons.Filled.RadioButtonUnchecked
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BlackButton(
                        label = if (loading) "Submitting..." else "Login",
                        onClick = { login() },
                        enabled = !loading && sequenceOf(server, email, password).all { it.isNotBlank() }
                    )

                    WhiteButton(
                        label = "Register",
                        onClick = onRegisterClick,
                        enabled = !loading
                    )
                }
            }
        }
    }
}
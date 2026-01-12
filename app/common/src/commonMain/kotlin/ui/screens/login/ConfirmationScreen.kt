package io.ktor.chat.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.chat.ui.components.*
import io.ktor.chat.utils.tryRequest
import io.ktor.chat.vm.ChatViewModel
import io.ktor.chat.vm.Confirmation

@Composable
fun ConfirmationScreen(vm: ChatViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val loadingState = remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val loading by loadingState
    var code by mutableStateOf(vm.confirmationCode)

    fun doConfirmation() {
        coroutineScope.tryRequest(loadingState, { error = it }) {
            vm.confirm(code)
        }
    }

    fun logout() {
        coroutineScope.tryRequest(loadingState, { error = it }) {
            vm.logout()
        }
    }

    WelcomeView {
        FormColumn {
            Text(
                buildAnnotatedString {
                    append("Please enter the ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                        append("confirmation code")
                    }
                    append(" we sent you:")
                },
                fontSize = 18.sp,
            )

            InputField(
                value = code,
                onValueChange = { code = it },
                placeholder = "Enter confirmation code",
                singleLine = true
            )

            error?.let {
                ErrorText(it, modifier = Modifier.align(Alignment.End))
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BlackButton(
                    label = if (loading) "Submitting..." else "Confirm",
                    onClick = ::doConfirmation,
                    enabled = !loading && code.isNotBlank()
                )

                WhiteButton(
                    label = "Cancel",
                    onClick = ::logout,
                    enabled = !loading
                )
            }
        }
    }
}

private val ChatViewModel.confirmationCode: String get() =
    (confirmation.value as? Confirmation.Pending)?.code.orEmpty()
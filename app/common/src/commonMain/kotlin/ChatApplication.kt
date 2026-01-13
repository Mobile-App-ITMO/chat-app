package io.ktor.chat

import androidx.compose.runtime.*
import androidx.compose.ui.layout.Layout
import io.ktor.chat.calls.VideoCallScreen
import io.ktor.chat.ui.screens.login.LoginScreen
import io.ktor.chat.ui.screens.login.RegisterScreen
import io.ktor.chat.ui.theme.ChatAppTheme
import io.ktor.chat.vm.ChatViewModel
import io.ktor.chat.vm.Confirmation
import io.ktor.chat.vm.VideoCallViewModel
import io.ktor.chat.vm.createViewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import io.ktor.chat.ui.screens.chat.GroupChatScreen
import io.ktor.chat.ui.screens.home.HomeScreen
import io.ktor.chat.ui.screens.login.ConfirmationScreen
import kotlinx.coroutines.launch

sealed class AppScreen {
    object Login : AppScreen()
    object Register : AppScreen()
    object Confirmation : AppScreen()
    object Home : AppScreen()
    object Chat : AppScreen()
    object VideoCall : AppScreen()
}

@Composable
fun ChatApplication(chatVm: ChatViewModel = createViewModel(), videoCallVm: VideoCallViewModel?, onRefreshSystemInfo: () -> Unit = {}) {
    val loggedInUser by remember { chatVm.loggedInUser }
    val confirmation by remember { chatVm.confirmation }
    val selectedRoom by remember { chatVm.room }
    val isInVideoCall by remember { videoCallVm?.isInVideoCall ?: mutableStateOf(false) }
    var screenSize by remember { chatVm.screenSize }
    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Login) }
    val navigationHistory = remember { mutableStateListOf<AppScreen>() }

    fun navigateTo(screen: AppScreen) {
        if (currentScreen != screen) {
            navigationHistory.add(currentScreen)
            currentScreen = screen
            println("Navigated from ${navigationHistory.lastOrNull()} to $screen")
        }
    }

    fun navigateBack() {
        if (navigationHistory.isNotEmpty()) {
            val previousScreen = navigationHistory.removeAt(navigationHistory.size - 1)
            currentScreen = previousScreen
            println("Navigated back to $previousScreen")
        } else {
            currentScreen = AppScreen.Home
        }
    }

    LaunchedEffect(loggedInUser) {
        if (loggedInUser != null) {
            videoCallVm?.user = loggedInUser
            videoCallVm?.init(this, chatVm.token.value!!)
            navigateTo(AppScreen.Home)
        } else {
            navigateTo(AppScreen.Login)
        }
    }

    LaunchedEffect(confirmation) {
        if (confirmation is Confirmation.Pending) {
            navigateTo(AppScreen.Confirmation)
        } else {
            if (currentScreen == AppScreen.Confirmation) {

                if (loggedInUser != null) {
                    navigateTo(AppScreen.Home)
                } else {
                    navigateTo(AppScreen.Login)
                }
            }
        }
    }

    LaunchedEffect(isInVideoCall) {
        if (isInVideoCall && videoCallVm != null) {
            navigateTo(AppScreen.VideoCall)
        } else if (loggedInUser != null && !isInVideoCall && currentScreen == AppScreen.VideoCall) {
            navigateBack()
        }
    }

    LaunchedEffect(selectedRoom) {
        if (selectedRoom != null && loggedInUser != null && currentScreen != AppScreen.Chat) {
            navigateTo(AppScreen.Chat)
        } else if (selectedRoom == null && currentScreen == AppScreen.Chat) {
            navigateTo(AppScreen.Home)
        }
    }

    LaunchedEffect(currentScreen) {
        when (currentScreen) {
            AppScreen.Home -> {
                if (selectedRoom != null) {
                    chatVm.room.value = null
                }
            }
            AppScreen.Login -> {
                chatVm.room.value = null
            }
            else -> {
                // для других скрины
            }
        }
    }

    ChatAppTheme {
        Layout(
            content = {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        when {
                            targetState == AppScreen.Register && initialState == AppScreen.Login ->
                                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) +
                                        fadeIn() togetherWith
                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left) +
                                        fadeOut()

                            targetState == AppScreen.Login && initialState == AppScreen.Register ->
                                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right) +
                                        fadeIn() togetherWith
                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) +
                                        fadeOut()

                            (targetState == AppScreen.Chat && initialState == AppScreen.Home) ||
                                    (targetState == AppScreen.Home && initialState == AppScreen.Chat) ->
                                fadeIn(animationSpec = tween(300)) togetherWith
                                        fadeOut(animationSpec = tween(300))

                            else ->
                                fadeIn(animationSpec = tween(300)) togetherWith
                                        fadeOut(animationSpec = tween(300))
                        }.using(SizeTransform(clip = false))
                    },
                    label = "Screen Transition"
                ) { targetScreen ->
                    when (targetScreen) {
                        AppScreen.Login -> {
                            LoginScreen(
                                vm = chatVm,
                                onRegisterClick = {
                                    navigateTo(AppScreen.Register)
                                }
                            )
                        }

                        AppScreen.Register -> {
                            RegisterScreen(
                                vm = chatVm,
                                onBack = {
                                    navigateTo(AppScreen.Login)
                                }
                            )
                        }

                        AppScreen.Confirmation -> {
                            ConfirmationScreen(
                                vm = chatVm
                            )
                        }

                        AppScreen.Home -> {
                            HomeScreen(
                                vm = chatVm,
                                videoCallVM = videoCallVm
                            )
                        }

                        AppScreen.Chat -> {
                            if (selectedRoom != null) {
                                GroupChatScreen(
                                    vm = chatVm,
                                    videoCallVM = videoCallVm,
                                    onBack = {
                                        coroutineScope.launch {
                                            chatVm.room.value = null
                                        }
                                    }
                                )
                            } else {
                                AppScreen.Home
                            }
                        }

                        AppScreen.VideoCall -> {
                            if (videoCallVm != null) {
                                VideoCallScreen(videoCallVm, chatVm)
                            } else {
                                navigateBack()
                            }
                        }
                    }
                }
            },

            measurePolicy = { measurables, constraints ->
                val width = constraints.maxWidth
                val height = constraints.maxHeight

                screenSize = Pair(width, height)

                val placeables = measurables.map { measurable ->
                    measurable.measure(constraints)
                }

                layout(width, height) {
                    var yPosition = 0
                    placeables.forEach { placeable ->
                        placeable.placeRelative(x = 0, y = yPosition)
                        yPosition += placeable.height
                    }
                }
            }
        )
    }
}
package com.example.general_first_aid_kit.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.general_first_aid_kit.presentation.screens.GreetingScreen
import com.example.general_first_aid_kit.presentation.screens.LoginScreen
import com.example.general_first_aid_kit.presentation.screens.MainScreen
import com.example.general_first_aid_kit.presentation.screens.RegistrationScreen
import com.example.general_first_aid_kit.presentation.viewmodels.AuthViewModel

@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val startRoute = remember {
        if (viewModel.isLogged()) Route.Main else Route.Welcome
    }

    val backStack = rememberNavBackStack(startRoute)

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key ->
            when (key) {
                is Route.Welcome -> NavEntry(key) {
                    GreetingScreen(
                        onNavigateToLogin = {
                            backStack.add(Route.Login)
                        },
                        onNavigateToRegister = {
                            backStack.add(Route.Register)
                        }
                    )
                }
                is Route.Login -> NavEntry(key) {
                    LoginScreen(
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onSuccess = {
                            backStack.clear()
                            backStack.add(Route.Main)
                        },
                        onForgotPasswordClick = {
                            //TODO: экран восстановления пароля
                        }
                    )
                }
                is Route.Register -> NavEntry(key) {
                    RegistrationScreen(
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onRegistrationClick = {
                            backStack.clear()
                            backStack.add(Route.Main)
                        }
                    )
                }
                is Route.Main -> NavEntry(key) {
                    MainScreen(
                        onLogout = {
                            viewModel.onSignOutClick()
                            backStack.clear()
                            backStack.add(Route.Welcome)
                        }
                    )
                }
                else -> error("Unknown key: $key")
            }
        }
    )
}
package com.example.general_first_aid_kit.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.general_first_aid_kit.presentation.screens.GreetingScreen
import com.example.general_first_aid_kit.presentation.screens.LoginScreen
import com.example.general_first_aid_kit.presentation.screens.MainScreen
import com.example.general_first_aid_kit.presentation.screens.RegistrationScreen

@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(Route.Welcome)

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
                        onLoginSuccess = {
                            backStack.clear()
                            backStack.add(Route.Main)
                        }
                    )
                }
                is Route.Register -> NavEntry(key) {
                    RegistrationScreen(
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onRegistrationSuccess = {
                            backStack.clear()
                            backStack.add(Route.Main)
                        }
                    )
                }
                is Route.Main -> NavEntry(key) {
                    MainScreen(
                        onLogout = {
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
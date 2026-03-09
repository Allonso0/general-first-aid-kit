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
import com.example.general_first_aid_kit.presentation.screens.CreateKitScreen
import com.example.general_first_aid_kit.presentation.screens.GreetingScreen
import com.example.general_first_aid_kit.presentation.screens.KitScreen
import com.example.general_first_aid_kit.presentation.screens.KitSettingsScreen
import com.example.general_first_aid_kit.presentation.screens.LoginScreen
import com.example.general_first_aid_kit.presentation.screens.MainScreen
import com.example.general_first_aid_kit.presentation.screens.ProfileScreen
import com.example.general_first_aid_kit.presentation.screens.ProfileSettingsScreen
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
                        onProfileClick = { backStack.add(Route.Profile) },
                        onAddKitClick = { backStack.add(Route.CreateKit) },
                        onKitCardClick = { id, name, location, colorIndex ->
                            backStack.add(Route.KitScreen(id, name, location, colorIndex))
                        }
                    )
                }
                is Route.Profile -> NavEntry(key) {
                    ProfileScreen(
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onLogout = {
                            viewModel.onSignOutClick()
                            backStack.clear()
                            backStack.add(Route.Welcome)
                        },
                        onNavigateToProfileSettings = {
                            backStack.add(Route.ProfileSettings)
                        }
                    )
                }
                is Route.ProfileSettings -> NavEntry(key) {
                    ProfileSettingsScreen(
                        onNavigateBack = { backStack.removeLastOrNull() }
                    )
                }
                is Route.CreateKit -> NavEntry(key) {
                    CreateKitScreen(
                        onNavigateBack = { backStack.removeLastOrNull() }
                    )
                }
                is Route.KitScreen -> NavEntry(key) {
                    KitScreen(
                        kitId = key.id,
                        kitName = key.name,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onNavigateToKitSettings = {
                            backStack.add(Route.KitSettings(key.id, key.name, key.location, key.colorIndex))
                        }
                    )
                }
                is Route.KitSettings -> NavEntry(key) {
                    KitSettingsScreen(
                        kitId = key.id,
                        initialName = key.name,
                        initialLocation = key.location,
                        initialColorIndex = key.colorIndex,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onSaveSuccess = {
                            backStack.clear()
                            backStack.add(Route.Main)
                        },
                        onDeleteSuccess = {
                            backStack.clear()
                            backStack.add(Route.Main)
                        }
                    )
                }
                else -> error("Unknown key: $key")
            }
        }
    )
}
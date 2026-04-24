package com.example.general_first_aid_kit.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.general_first_aid_kit.presentation.screens.AddMedicationScreen
import com.example.general_first_aid_kit.presentation.screens.CreateKitScreen
import com.example.general_first_aid_kit.presentation.screens.EditMedicationScreen
import com.example.general_first_aid_kit.presentation.screens.GreetingScreen
import com.example.general_first_aid_kit.presentation.screens.JoinKitScreen
import com.example.general_first_aid_kit.presentation.screens.KitScreen
import com.example.general_first_aid_kit.presentation.screens.KitSettingsScreen
import com.example.general_first_aid_kit.presentation.screens.LoginScreen
import com.example.general_first_aid_kit.presentation.screens.MainScreen
import com.example.general_first_aid_kit.presentation.screens.MedicationInfoScreen
import com.example.general_first_aid_kit.presentation.screens.ProfileScreen
import com.example.general_first_aid_kit.presentation.screens.ProfileSettingsScreen
import com.example.general_first_aid_kit.presentation.screens.RegistrationScreen
import com.example.general_first_aid_kit.presentation.screens.NotificationLogScreen
import com.example.general_first_aid_kit.presentation.screens.ScanBarcodeScreen
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
        onBack = {
            backStack.pop()
        },
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
                        onNavigateBack = { backStack.pop() },
                        onSuccess = {
                            backStack.setStack(Route.Main)
                        },
                        onForgotPasswordClick = {
                            //TODO: экран восстановления пароля
                        }
                    )
                }
                is Route.Register -> NavEntry(key) {
                    RegistrationScreen(
                        onNavigateBack = { backStack.pop() },
                        onRegistrationClick = {
                            backStack.setStack(Route.Main)
                        }
                    )
                }
                is Route.Main -> NavEntry(key) {
                    MainScreen(
                        onProfileClick = { backStack.add(Route.Profile) },
                        onJoinClick = { backStack.add(Route.JoinKit) },
                        onAddKitClick = { backStack.add(Route.CreateKit) },
                        onKitCardClick = { id, name, location, colorIndex ->
                            backStack.add(Route.KitScreen(id, name, location, colorIndex))
                        }
                    )
                }
                is Route.Profile -> NavEntry(key) {
                    ProfileScreen(
                        onNavigateBack = { backStack.pop() },
                        onLogout = {
                            viewModel.onSignOutClick()
                            backStack.setStack(Route.Welcome)
                        },
                        onNavigateToProfileSettings = {
                            backStack.add(Route.ProfileSettings)
                        },
                        onNavigateToNotificationLog = {
                            backStack.add(Route.NotificationLog)
                        }
                    )
                }
                is Route.ProfileSettings -> NavEntry(key) {
                    ProfileSettingsScreen(
                        onNavigateBack = { backStack.pop() }
                    )
                }
                is Route.CreateKit -> NavEntry(key) {
                    CreateKitScreen(
                        onNavigateBack = { backStack.pop() }
                    )
                }
                is Route.KitScreen -> NavEntry(key) {
                    KitScreen(
                        kitId = key.id,
                        kitName = key.name,
                        onNavigateBack = { backStack.pop() },
                        onNavigateToKitSettings = {
                            backStack.add(Route.KitSettings(key.id, key.name, key.location, key.colorIndex))
                        },
                        onNavigateToAddManual = { backStack.add(Route.AddMedicationManual(key.id)) },
                        onScanBarcode = { backStack.add(Route.ScanBarcode(key.id)) },
                        onNavigateToMedicationInfo = { medId ->
                            backStack.add(Route.MedicationInfo(key.id, medId))
                        }
                    )
                }
                is Route.KitSettings -> NavEntry(key) {
                    KitSettingsScreen(
                        kitId = key.id,
                        initialName = key.name,
                        initialLocation = key.location,
                        initialColorIndex = key.colorIndex,
                        onNavigateBack = { backStack.pop() },
                        onSaveSuccess = {
                            backStack.setStack(Route.Main)
                        },
                        onDeleteSuccess = {
                            backStack.setStack(Route.Main)
                        }
                    )
                }
                is Route.AddMedicationManual -> NavEntry(key) {
                    AddMedicationScreen(
                        kitId = key.kitId,
                        scannedBarcode = key.scannedBarcode,
                        onNavigateBack = { backStack.pop() }
                    )
                }
                is Route.MedicationInfo -> NavEntry(key) {
                    MedicationInfoScreen(
                        kitId = key.kitId,
                        medicationId = key.medicationId,
                        onNavigateBack = { backStack.pop() },
                        onNavigateToEdit = { kitId, medId ->
                            backStack.add(Route.EditMedication(kitId, medId))
                        }
                    )
                }
                is Route.EditMedication -> NavEntry(key) {
                    EditMedicationScreen(
                        kitId = key.kitId,
                        medicationId = key.medicationId,
                        onNavigateBack = { backStack.pop() },
                        onDeleteSuccess = {
                            backStack.pop()
                            backStack.pop()
                        }
                    )
                }
                is Route.NotificationLog -> NavEntry(key) {
                    NotificationLogScreen(onNavigateBack = { backStack.pop() })
                }
                is Route.JoinKit -> NavEntry(key) {
                    JoinKitScreen(onNavigateBack = { backStack.pop() })
                }
                is Route.ScanBarcode -> NavEntry(key) {
                    ScanBarcodeScreen(
                        kitId = key.kitId,
                        onNavigateBack = { backStack.pop() },
                        onBarcodeScanned = { barcode ->
                            backStack.pop()
                            backStack.add(Route.AddMedicationManual(key.kitId, barcode))
                        }
                    )
                }
                else -> error("Unknown key: $key")
            }
        }
    )
}

private fun <T> MutableList<T>.pop() {
    if (size > 1) {
        removeAt(size - 1)
    }
}

private fun <T> MutableList<T>.setStack(route: T) {
    Snapshot.withMutableSnapshot {
        add(route)
        while (size > 1) {
            removeAt(0)
        }
    }
}

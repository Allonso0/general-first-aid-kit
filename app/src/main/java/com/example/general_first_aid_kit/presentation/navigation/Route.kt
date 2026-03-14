package com.example.general_first_aid_kit.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route: NavKey {

    @Serializable
    data object Welcome: Route

    @Serializable
    data object Login: Route

    @Serializable
    data object Register: Route

    @Serializable
    data object Main: Route

    @Serializable
    data object Profile: Route

    @Serializable
    data object ProfileSettings: Route

    @Serializable
    data object CreateKit : Route

    @Serializable
    data class KitScreen(val id: String, val name: String, val location: String, val colorIndex: Int): Route

    @Serializable
    data class KitSettings(val id: String, val name: String, val location: String, val colorIndex: Int): Route

    @Serializable
    data class AddMedicationManual(val kitId: String) : Route
}
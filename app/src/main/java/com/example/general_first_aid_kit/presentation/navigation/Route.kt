package com.example.general_first_aid_kit.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route: NavKey {

    @Serializable
    data object Welcome: Route, NavKey

    @Serializable
    data object Login: Route, NavKey

    @Serializable
    data object Register: Route, NavKey

    @Serializable
    data object Main: Route, NavKey
}
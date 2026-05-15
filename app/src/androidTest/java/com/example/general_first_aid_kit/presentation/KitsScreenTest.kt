package com.example.general_first_aid_kit.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.general_first_aid_kit.data.connectivity.ConnectivityMonitor
import com.example.general_first_aid_kit.domain.model.AppSettings
import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.usecase.DeleteKitUseCase
import com.example.general_first_aid_kit.domain.usecase.GetAllMedicationsUseCase
import com.example.general_first_aid_kit.domain.usecase.GetAppSettingsUseCase
import com.example.general_first_aid_kit.domain.usecase.GetKitsUseCase
import com.example.general_first_aid_kit.presentation.screens.MainScreen
import com.example.general_first_aid_kit.presentation.viewmodels.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KitsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val getKitsUseCase = mockk<GetKitsUseCase>()
    private val deleteKitUseCase = mockk<DeleteKitUseCase>(relaxed = true)
    private val getAllMedicationsUseCase = mockk<GetAllMedicationsUseCase>()
    private val getAppSettingsUseCase = mockk<GetAppSettingsUseCase>()
    private val auth = mockk<FirebaseAuth>(relaxed = true)
    private val connectivityMonitor = mockk<ConnectivityMonitor>()

    @Before
    fun setUp() {
        every { connectivityMonitor.isOnline } returns MutableStateFlow(true)
        every { getAppSettingsUseCase() } returns AppSettings()
        every { auth.currentUser } returns null
    }

    @Test
    fun kits_areDisplayed_when_dataLoadsSuccessfully() {
        every { getKitsUseCase() } returns flowOf(listOf(fakeKit()))
        every { getAllMedicationsUseCase() } returns flowOf(emptyList<Medication>())

        composeTestRule.setContent {
            MainScreen(
                onProfileClick = {},
                onJoinClick = {},
                onAddKitClick = {},
                onKitCardClick = { _, _, _, _, _ -> },
                viewModel = createViewModel()
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule.onAllNodesWithText("Домашняя аптечка")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Домашняя аптечка").assertIsDisplayed()
    }

    @Test
    fun loadingIndicator_isDisplayed_when_dataIsLoading() {
        every { getKitsUseCase() } returns MutableSharedFlow<List<Kit>>()
        every { getAllMedicationsUseCase() } returns MutableSharedFlow<List<Medication>>()

        composeTestRule.setContent {
            MainScreen(
                onProfileClick = {},
                onJoinClick = {},
                onAddKitClick = {},
                onKitCardClick = { _, _, _, _, _ -> },
                viewModel = createViewModel()
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    private fun createViewModel() = MainViewModel(
        getKitsUseCase, deleteKitUseCase, getAllMedicationsUseCase,
        getAppSettingsUseCase, auth, connectivityMonitor
    )

    private fun fakeKit() = Kit(
        id = "kit-1",
        name = "Домашняя аптечка",
        location = "Дома",
        colorIndex = 0,
        type = KitType.PERSONAL,
        userIds = listOf("user-1"),
        archivedUserIds = emptyList()
    )
}

package com.example.general_first_aid_kit.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.component.ColorSelectionDialog
import com.example.general_first_aid_kit.presentation.component.GenericTabRow
import com.example.general_first_aid_kit.presentation.component.KitColorPreview
import com.example.general_first_aid_kit.presentation.component.KitInputField
import com.example.general_first_aid_kit.presentation.component.KitSectionTitle
import com.example.general_first_aid_kit.presentation.component.KitTypeRadioButton
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.TextGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextRed
import com.example.general_first_aid_kit.presentation.ui.theme.White
import com.example.general_first_aid_kit.presentation.viewmodels.KitSettingsEvent
import com.example.general_first_aid_kit.presentation.viewmodels.KitSettingsUiState
import com.example.general_first_aid_kit.presentation.viewmodels.KitSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitSettingsScreen(
    kitId: String,
    initialName: String,
    initialLocation: String,
    initialColorIndex: Int,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    onDeleteSuccess: () -> Unit,
    viewModel: KitSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showColorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initScreen(initialName, initialLocation, initialColorIndex)
    }

    if (showColorDialog) {
        ColorSelectionDialog(
            onDismiss = { showColorDialog = false },
            onColorSelected = {
                viewModel.onEvent(KitSettingsEvent.ColorSelected(it))
                showColorDialog = false
            }
        )
    }

    Scaffold(
        containerColor = White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    text = "Информация",
                    style = MaterialTheme.typography.titleLarge,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = GreenPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveChanges(kitId, onSuccess = onSaveSuccess) },
                        enabled = !state.isLoading
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_check_24),
                            contentDescription = "Сохранить",
                            tint = GreenPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            GenericTabRow(
                selectedTabIndex = state.selectedTab,
                tabs = listOf("Настройки", "Участники"),
                onTabSelected = { viewModel.onEvent(KitSettingsEvent.TabChanged(it)) }
            )

            if (state.selectedTab == 0) {
                SettingsTabContent(
                    state = state,
                    onEvent = viewModel::onEvent,
                    onShowColorDialog = { showColorDialog = true },
                    onDeleteClick = { viewModel.deleteKit(kitId, onSuccess = onDeleteSuccess) }
                )
            } else {
                ParticipantsTabContent()
            }
        }
    }
}

@Composable
fun SettingsTabContent(
    state: KitSettingsUiState,
    onEvent: (KitSettingsEvent) -> Unit,
    onShowColorDialog: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.PaddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
    ) {
        KitSectionTitle("Цвет обложки")
        KitColorPreview(colorIndex = state.selectedColorIndex, onChooseClick = onShowColorDialog)

        KitSectionTitle("Основная информация")
        KitInputField(value = state.name, onValueChange = { onEvent(KitSettingsEvent.NameChanged(it)) }, label = "Название")
        KitInputField(value = state.location, onValueChange = { onEvent(KitSettingsEvent.LocationChanged(it)) }, label = "Местоположение")

        KitSectionTitle("Тип аптечки")
        KitTypeRadioButton(
            selected = !state.isPublic,
            title = "Личная аптечка",
            subtitle = "Только вы видите содержимое",
            onClick = { onEvent(KitSettingsEvent.TogglePublic(false)) }
        )
        KitTypeRadioButton(
            selected = state.isPublic,
            title = "Общая аптечка",
            subtitle = "Доступ по приглашению",
            onClick = { onEvent(KitSettingsEvent.TogglePublic(true)) }
        )

        Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
        
        Button(
            onClick = { /* Archive */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary.copy(alpha = 0.1f), contentColor = GreenPrimary),
            shape = RoundedCornerShape(Dimensions.CornerRadiusMedium)
        ) {
            Text("Архивировать аптечку")
        }

        Button(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFEBEE),
                contentColor = TextRed
            ),
            shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
            enabled = !state.isLoading
        ) {
            Text("Удалить аптечку", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ParticipantsTabContent() {}
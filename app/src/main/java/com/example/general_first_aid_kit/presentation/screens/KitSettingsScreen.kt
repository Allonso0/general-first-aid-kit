package com.example.general_first_aid_kit.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.component.*
import com.example.general_first_aid_kit.presentation.ui.theme.*
import com.example.general_first_aid_kit.presentation.utils.shimmerEffect
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
    var showToPublicDialog by remember { mutableStateOf(false) }
    var showToPersonalDialog by remember { mutableStateOf(false) }

    var showColorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(kitId) {
        viewModel.initScreen(kitId = kitId, initialName, initialLocation, initialColorIndex)
    }

    LaunchedEffect(state.selectedTab) {
        if (state.selectedTab == 1) {
            viewModel.initScreen(kitId, state.name, state.location, state.selectedColorIndex)
        }
    }

    LaunchedEffect(state.isKitDeleted) {
        if (state.isKitDeleted && !state.isOwner) {
            onDeleteSuccess()
        }
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

    if (showToPublicDialog) {
        ChangeToPublicDialog(
            onConfirm = {
                viewModel.onEvent(KitSettingsEvent.TogglePublic(true))
                showToPublicDialog = false
            },
            onDismiss = { showToPublicDialog = false }
        )
    }

    if (showToPersonalDialog) {
        ChangeToPersonalDialog(
            onConfirm = {
                viewModel.onEvent(KitSettingsEvent.TogglePublic(false))
                showToPersonalDialog = false
            },
            onDismiss = { showToPersonalDialog = false }
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
                    if (state.selectedTab == 0 && state.isOwner) {
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
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
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
                    onDeleteClick = { viewModel.deleteKit(kitId, onSuccess = onDeleteSuccess) },
                    onLeaveClick = { viewModel.leaveKit(onSuccess = onDeleteSuccess) },
                    onToggleTypeClick = { isPublicTarget ->
                        if (isPublicTarget) showToPublicDialog = true
                        else showToPersonalDialog = true
                    }
                )
            } else {
                ParticipantsTabContent(
                    state = state,
                    onGenerateCode = { viewModel.generateInviteCode(kitId) },
                    onRemoveParticipant = viewModel::removeParticipant
                )
            }
        }
    }
}

@Composable
fun SettingsTabContent(
    state: KitSettingsUiState,
    onEvent: (KitSettingsEvent) -> Unit,
    onShowColorDialog: () -> Unit,
    onDeleteClick: () -> Unit,
    onLeaveClick: () -> Unit,
    onToggleTypeClick: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.PaddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
    ) {
        KitSectionTitle("Цвет обложки")
        KitColorPreview(
            colorIndex = state.selectedColorIndex,
            onChooseClick = if (state.isOwner) onShowColorDialog else null
        )

        KitSectionTitle("Основная информация")
        KitInputField(
            value = state.name,
            onValueChange = { onEvent(KitSettingsEvent.NameChanged(it)) },
            label = "Название",
            focusedTextColor = if (state.isOwner) TextBlack else TextGray,
            enabled = state.isOwner
        )
        KitInputField(
            value = state.location,
            onValueChange = { onEvent(KitSettingsEvent.LocationChanged(it)) },
            label = "Местоположение",
            focusedTextColor = if (state.isOwner) TextBlack else TextGray,
            enabled = state.isOwner
        )

        KitSectionTitle("Тип аптечки")
        KitTypeRadioButton(
            selected = !state.isPublic,
            title = "Личная аптечка",
            subtitle = "Только вы видите содержимое",
            onClick = {
                if (state.isOwner && state.isPublic) onToggleTypeClick(false)
            }
        )
        KitTypeRadioButton(
            selected = state.isPublic,
            title = "Общая аптечка",
            subtitle = "Доступ по приглашению",
            onClick = {
                if (state.isOwner && !state.isPublic) onToggleTypeClick(true)
            }
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

        if (state.isOwner) {
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
        } else {
            Button(
                onClick = onLeaveClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEBEE),
                    contentColor = TextRed
                ),
                shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
                enabled = !state.isLoading
            ) {
                Text("Покинуть аптечку", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ParticipantsTabContent(
    state: KitSettingsUiState,
    onGenerateCode: () -> Unit,
    onRemoveParticipant: (String) -> Unit
) {
    var showInviteDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!state.isPublic) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.PaddingExtraLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_lock_24),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = LightGray
                )
                Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
                Text(
                    text = "Это личная аптечка",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack
                )
                Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
                Text(
                    text = "Список участников доступен только для общих аптечек. Вы можете изменить тип аптечки во вкладке «Настройки».",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading && state.participants.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(state.participants, key = { it.id }) { user ->
                            val isSelf = user.id == state.currentUserId
                            val isUserOwner = user.id == state.ownerId

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape,
                                    color = GreenPrimary.copy(alpha = 0.1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (!user.avatarURL.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = user.avatarURL,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize().shimmerEffect(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Text(
                                                text = user.name.take(1).uppercase(),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = GreenPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .weight(1f)
                                ) {
                                    Text(
                                        text = if (isSelf) "${user.name} (Вы)" else user.name,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextBlack,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    if (isUserOwner) {
                                        Text(
                                            text = "Владелец",
                                            color = GreenPrimary,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }

                                if (state.isOwner && !isUserOwner) {
                                    IconButton(onClick = { onRemoveParticipant(user.id) }) {
                                        Icon(
                                            painter = painterResource(R.drawable.baseline_delete_24),
                                            contentDescription = "Удалить",
                                            tint = TextRed.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.isOwner) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Button(
                            onClick = {
                                if (state.inviteCode == null) onGenerateCode()
                                showInviteDialog = true
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(Dimensions.CornerRadiusMedium)
                        ) {
                            Icon(painterResource(R.drawable.baseline_group_24), null, tint = White)
                            Spacer(Modifier.width(8.dp))
                            Text("Пригласить участника", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            containerColor = White,
            title = {
                Text(
                    "Код приглашения",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Передайте этот 8-значный код пользователю:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))

                    Surface(
                        color = LightGray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LightGray.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = state.inviteCode ?: "Генерация...",
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = GreenPrimary,
                            letterSpacing = 4.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    TextButton(
                        onClick = onGenerateCode,
                        colors = ButtonDefaults.textButtonColors(contentColor = GreenPrimary)
                    ) {
                        Icon(painterResource(R.drawable.baseline_refresh_24), null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Обновить код")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInviteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimensions.CornerRadiusMedium)
                ) {
                    Text("Готово", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

package com.example.general_first_aid_kit.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.component.*
import com.example.general_first_aid_kit.presentation.ui.theme.*
import com.example.general_first_aid_kit.presentation.utils.shimmerEffect
import com.example.general_first_aid_kit.presentation.viewmodels.KitSettingsEvent
import com.example.general_first_aid_kit.presentation.viewmodels.KitSettingsUiState
import com.example.general_first_aid_kit.presentation.viewmodels.KitSettingsViewModel
import com.example.general_first_aid_kit.presentation.viewmodels.NotificationSetting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitSettingsScreen(
    kitId: String,
    initialName: String,
    initialLocation: String,
    initialColorIndex: Int,
    initialIsPublic: Boolean,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    onDeleteSuccess: () -> Unit,
    onArchiveSuccess: () -> Unit,
    viewModel: KitSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isSharedAndOffline by remember { derivedStateOf { state.isPublic && !isOnline } }

    val tabSettings = stringResource(R.string.settings)
    val tabParticipants = stringResource(R.string.tab_participants)
    val tabNotifications = stringResource(R.string.tab_kit_notifications)

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    var showToPublicDialog by remember { mutableStateOf(false) }
    var showToPersonalDialog by remember { mutableStateOf(false) }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showLeaveConfirm by remember { mutableStateOf(false) }
    var showArchiveConfirm by remember { mutableStateOf(false) }

    var showColorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(kitId) {
        viewModel.initScreen(kitId = kitId, initialName, initialLocation, initialColorIndex, initialIsPublic)
    }

    LaunchedEffect(state.selectedTab) {
        if (state.selectedTab == 1) {
            viewModel.initScreen(kitId, state.name, state.location, state.selectedColorIndex, state.isPublic)
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

    if (showDeleteConfirm) {
        DeleteKitConfirmationDialog(
            onConfirm = {
                viewModel.deleteKit(onSuccess = onDeleteSuccess)
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    if (showLeaveConfirm) {
        LeaveKitConfirmationDialog(
            onConfirm = {
                viewModel.leaveKit(onSuccess = onDeleteSuccess)
                showLeaveConfirm = false
            },
            onDismiss = { showLeaveConfirm = false }
        )
    }

    if (showArchiveConfirm) {
        if (state.isArchived) {
            UnarchiveKitConfirmationDialog(
                onConfirm = {
                    viewModel.setArchived(false, onArchiveSuccess)
                    showArchiveConfirm = false
                },
                onDismiss = { showArchiveConfirm = false }
            )
        } else {
            ArchiveKitConfirmationDialog(
                onConfirm = {
                    viewModel.setArchived(true, onArchiveSuccess)
                    showArchiveConfirm = false
                },
                onDismiss = { showArchiveConfirm = false }
            )
        }
    }

    Scaffold(
        containerColor = White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    text = stringResource(R.string.kit_info_title),
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
                            onClick = { viewModel.saveChanges(onSuccess = onSaveSuccess) },
                            enabled = !state.isLoading
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_check_24),
                                contentDescription = stringResource(R.string.save),
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
            if (isSharedAndOffline) {
                OfflineBanner(stringResource(R.string.offline_banner_shared_kit))
            }

            GenericTabRow(
                selectedTabIndex = state.selectedTab,
                tabs = listOf(tabSettings, tabParticipants, tabNotifications),
                onTabSelected = { viewModel.onEvent(KitSettingsEvent.TabChanged(it)) }
            )

            if (!state.isInitialized) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else {
                when (state.selectedTab) {
                    0 -> SettingsTabContent(
                        state = state,
                        onEvent = viewModel::onEvent,
                        onShowColorDialog = { showColorDialog = true },
                        onDeleteClick = { showDeleteConfirm = true },
                        onLeaveClick = { showLeaveConfirm = true },
                        onArchiveClick = { showArchiveConfirm = true },
                        onToggleTypeClick = { isPublicTarget ->
                            if (isPublicTarget) showToPublicDialog = true
                            else showToPersonalDialog = true
                        }
                    )
                    1 -> ParticipantsTabContent(
                        state = state,
                        onGenerateCode = { viewModel.generateInviteCode() },
                        onRemoveParticipant = viewModel::removeParticipant
                    )
                    else -> NotificationsTabContent(
                        state = state,
                        onEvent = viewModel::onEvent
                    )
                }
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
    onArchiveClick: () -> Unit,
    onToggleTypeClick: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.PaddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
    ) {
        KitSectionTitle(stringResource(R.string.cover_color))
        KitColorPreview(
            colorIndex = state.selectedColorIndex,
            onChooseClick = if (state.isOwner) onShowColorDialog else null
        )

        KitSectionTitle(stringResource(R.string.main_info))
        KitInputField(
            value = state.name,
            onValueChange = { onEvent(KitSettingsEvent.NameChanged(it)) },
            label = stringResource(R.string.label_name),
            focusedTextColor = if (state.isOwner) TextBlack else TextGray,
            enabled = state.isOwner
        )
        KitInputField(
            value = state.location,
            onValueChange = { onEvent(KitSettingsEvent.LocationChanged(it)) },
            label = stringResource(R.string.location),
            focusedTextColor = if (state.isOwner) TextBlack else TextGray,
            enabled = state.isOwner
        )

        KitSectionTitle(stringResource(R.string.kit_type))
        KitTypeRadioButton(
            selected = !state.isPublic,
            title = stringResource(R.string.personal_kit),
            subtitle = stringResource(R.string.personal_kit_only_you),
            onClick = {
                if (state.isOwner && state.isPublic) onToggleTypeClick(false)
            }
        )
        KitTypeRadioButton(
            selected = state.isPublic,
            title = stringResource(R.string.public_kit),
            subtitle = stringResource(R.string.public_kit_by_invite),
            onClick = {
                if (state.isOwner && !state.isPublic) onToggleTypeClick(true)
            }
        )

        Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

        OutlinedButton(
            onClick = onArchiveClick,
            modifier = Modifier.fillMaxWidth().height(Dimensions.MediumButtonHeight),
            border = BorderStroke(1.dp, GreenPrimary),
            shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary)
        ) {
            Text(
                text = if (state.isArchived) stringResource(R.string.kit_unarchive) else stringResource(R.string.kit_archive),
                fontWeight = FontWeight.SemiBold
            )
        }

        if (state.isOwner) {
            OutlinedButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.MediumButtonHeight),
                border = BorderStroke(1.dp, TextRed),
                shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextRed)
            ) {
                Text(stringResource(R.string.action_delete_kit), fontWeight = FontWeight.Bold)
            }
        } else {
            OutlinedButton(
                onClick = onLeaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.MediumButtonHeight),
                border = BorderStroke(1.dp, TextRed),
                shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextRed)
            ) {
                Text(stringResource(R.string.action_leave_kit), fontWeight = FontWeight.Bold)
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
                    text = stringResource(R.string.personal_kit_empty_state),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
                Text(
                    text = stringResource(R.string.participants_unavailable_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
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
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .shimmerEffect(),
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
                                            text = stringResource(R.string.role_owner),
                                            color = GreenPrimary,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }

                                if (state.isOwner && !isUserOwner) {
                                    IconButton(onClick = { onRemoveParticipant(user.id) }) {
                                        Icon(
                                            painter = painterResource(R.drawable.baseline_delete_24),
                                            contentDescription = stringResource(R.string.delete),
                                            tint = TextRed.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                if (state.isOwner) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)) {
                        Button(
                            onClick = {
                                if (state.inviteCode == null) onGenerateCode()
                                showInviteDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(Dimensions.CornerRadiusMedium)
                        ) {
                            Icon(painterResource(R.drawable.baseline_group_24), null, tint = White)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.action_invite_participant),
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                    }
                }
            }
        }
    }

    if (showInviteDialog) {
        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            containerColor = White,
            title = {
                Text(
                    text = stringResource(R.string.invite_code_title),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.invite_code_instruction),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))

                    Surface(
                        color = LightGray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, LightGray.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = state.inviteCode ?: stringResource(R.string.invite_code_generating),
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = GreenPrimary,
                            letterSpacing = 4.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    val codeCopiedMessage = stringResource(R.string.message_code_copied)
                    TextButton(
                        onClick = {
                            state.inviteCode?.let { code ->
                                val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                                clipboard?.setPrimaryClip(android.content.ClipData.newPlainText(null, code))
                                android.widget.Toast.makeText(
                                    context,
                                    codeCopiedMessage,
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = state.inviteCode != null,
                        colors = ButtonDefaults.textButtonColors(contentColor = GreenPrimary)
                    ) {
                        Icon(painterResource(R.drawable.baseline_content_copy_24), null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.action_copy_code))
                    }

                    TextButton(
                        onClick = onGenerateCode,
                        colors = ButtonDefaults.textButtonColors(contentColor = GreenPrimary)
                    ) {
                        Icon(painterResource(R.drawable.baseline_refresh_24), null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.action_refresh_code))
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
                    Text(
                        text = stringResource(R.string.action_done),
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }
        )
    }
}

@Composable
fun NotificationsTabContent(
    state: KitSettingsUiState,
    onEvent: (KitSettingsEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.PaddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingExtraSmall)
    ) {
        KitSectionTitle(stringResource(R.string.notif_kit_section_title))

        NotificationCheckboxItem(
            checked = state.notifyExpiry,
            text = stringResource(R.string.notif_expiry_checkbox),
            onCheckedChange = {
                onEvent(KitSettingsEvent.NotificationSettingChanged(NotificationSetting.EXPIRY, it))
            }
        )

        NotificationCheckboxItem(
            checked = state.notifyLowStock,
            text = stringResource(R.string.notif_low_stock_checkbox),
            onCheckedChange = {
                onEvent(KitSettingsEvent.NotificationSettingChanged(NotificationSetting.LOW_STOCK, it))
            }
        )

        if (state.isPublic) {
            NotificationCheckboxItem(
                checked = state.notifyMemberActivity,
                text = stringResource(R.string.notif_member_activity_checkbox),
                onCheckedChange = {
                    onEvent(KitSettingsEvent.NotificationSettingChanged(NotificationSetting.MEMBER_ACTIVITY, it))
                }
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

        Text(
            text = stringResource(R.string.notif_settings_note),
            style = MaterialTheme.typography.labelSmall,
            color = TextGray
        )
    }
}

@Composable
private fun NotificationCheckboxItem(
    checked: Boolean,
    text: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = Dimensions.PaddingExtraSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = GreenPrimary,
                checkmarkColor = White
            )
        )
        Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextBlack
        )
    }
}

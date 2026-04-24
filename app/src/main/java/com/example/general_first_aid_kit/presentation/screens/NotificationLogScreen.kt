package com.example.general_first_aid_kit.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.domain.model.AppNotification
import com.example.general_first_aid_kit.domain.model.NotificationType
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.GreenSecondary
import com.example.general_first_aid_kit.presentation.ui.theme.KitColorOrange
import com.example.general_first_aid_kit.presentation.ui.theme.KitColorRed
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.TextGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextGreen
import com.example.general_first_aid_kit.presentation.ui.theme.White
import com.example.general_first_aid_kit.presentation.viewmodels.NotificationLogViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationLogScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.notifications_log),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextGreen,
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = GreenPrimary)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = stringResource(R.string.back_button_description)
                        )
                    }
                },
                actions = {
                    if (uiState.notifications.any { !it.isRead }) {
                        TextButton(onClick = viewModel::onMarkAllRead) {
                            Text(
                                text = "Прочитано",
                                color = GreenPrimary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        containerColor = White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = GreenPrimary
                    )
                }
                uiState.notifications.isEmpty() -> {
                    Text(
                        text = "Уведомлений пока нет",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextGray
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.notifications, key = { it.id }) { notification ->
                            NotificationItem(notification = notification)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(notification: AppNotification) {
    val dateFormatter = SimpleDateFormat("d MMM, HH:mm", Locale("ru"))
    val formattedDate = dateFormatter.format(Date(notification.timestamp))

    val backgroundColor = if (!notification.isRead) {
        GreenSecondary.copy(alpha = 0.18f)
    } else {
        White
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingMedium),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(notificationIconBackground(notification.type)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(notificationIcon(notification.type)),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = White
            )
        }

        Spacer(modifier = Modifier.width(Dimensions.SpacingMedium))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextBlack,
                fontWeight = if (!notification.isRead) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = TextGray
            )
        }
    }
}

private fun notificationIcon(type: NotificationType): Int = when (type) {
    NotificationType.EXPIRY_WARNING -> R.drawable.baseline_calendar_today_24
    NotificationType.EXPIRED -> R.drawable.baseline_close_24
    NotificationType.LOW_STOCK -> R.drawable.baseline_medication_24
    NotificationType.MEMBER_JOINED -> R.drawable.baseline_person_add_24
    NotificationType.MEMBER_LEFT -> R.drawable.baseline_group_24
    NotificationType.MEMBER_ADDED_MEDICATION -> R.drawable.baseline_add_24
    NotificationType.MEMBER_REMOVED_MEDICATION -> R.drawable.baseline_delete_24
    NotificationType.MEMBER_EDITED_MEDICATION -> R.drawable.baseline_edit_note_24
}

private fun notificationIconBackground(type: NotificationType) = when (type) {
    NotificationType.EXPIRY_WARNING -> KitColorOrange
    NotificationType.EXPIRED -> KitColorRed
    NotificationType.LOW_STOCK -> KitColorOrange
    NotificationType.MEMBER_JOINED,
    NotificationType.MEMBER_LEFT,
    NotificationType.MEMBER_ADDED_MEDICATION,
    NotificationType.MEMBER_REMOVED_MEDICATION,
    NotificationType.MEMBER_EDITED_MEDICATION -> GreenPrimary
}

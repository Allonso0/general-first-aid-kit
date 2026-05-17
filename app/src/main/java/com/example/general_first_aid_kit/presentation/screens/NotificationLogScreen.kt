package com.example.general_first_aid_kit.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.domain.model.AppNotification
import com.example.general_first_aid_kit.domain.model.NotificationType
import com.example.general_first_aid_kit.presentation.ui.theme.ButtonRed
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.GreenSecondary
import com.example.general_first_aid_kit.presentation.ui.theme.KitColorOrange
import com.example.general_first_aid_kit.presentation.ui.theme.KitColorRed
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.TextGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextGreen
import com.example.general_first_aid_kit.presentation.ui.theme.TextRed
import com.example.general_first_aid_kit.presentation.ui.theme.TextWhite
import com.example.general_first_aid_kit.presentation.ui.theme.White
import com.example.general_first_aid_kit.presentation.viewmodels.NotificationLogViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationLogScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val firstId = uiState.notifications.firstOrNull()?.id
    var previousFirstId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(firstId) {
        if (firstId != null && previousFirstId != null && firstId != previousFirstId
            && listState.firstVisibleItemIndex <= 1
        ) {
            listState.animateScrollToItem(0)
        }
        previousFirstId = firstId
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = White,
            title = {
                Text(
                    text = stringResource(R.string.notif_clear_log),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextBlack
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.notif_clear_log_confirm_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDeleteAll()
                    showDeleteDialog = false
                }) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = TextRed,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = GreenPrimary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        )
    }

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
                    if (uiState.notifications.isNotEmpty()) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = GreenPrimary)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_delete_24),
                                contentDescription = stringResource(R.string.notif_clear_log)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        bottomBar = {
            if (uiState.notifications.any { !it.isRead }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingMedium),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = viewModel::onMarkAllRead,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimensions.LargeButtonHeight),
                        shape = RoundedCornerShape(Dimensions.CornerRadiusLarge),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary,
                            contentColor = TextWhite
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.notif_mark_all_read),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
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
                        text = stringResource(R.string.notif_empty),
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextGray
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
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
    val todayPrefix = stringResource(R.string.notif_time_today_prefix)
    val yesterdayPrefix = stringResource(R.string.notif_time_yesterday_prefix)
    val formattedDate = remember(notification.timestamp, todayPrefix, yesterdayPrefix) {
        formatNotificationTime(notification.timestamp, todayPrefix, yesterdayPrefix)
    }

    val backgroundColor = if (!notification.isRead) UnreadBackground else White

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

private val UnreadBackground = GreenSecondary.copy(alpha = 0.18f)

private val moscowTz = TimeZone.getTimeZone("Europe/Moscow")

private fun formatNotificationTime(timestamp: Long, todayPrefix: String, yesterdayPrefix: String): String {
    if (timestamp <= 0L) return ""

    val timeFormat = SimpleDateFormat("HH:mm", Locale("ru")).apply { timeZone = moscowTz }
    val calTs = Calendar.getInstance(moscowTz).apply { timeInMillis = timestamp }
    val today = Calendar.getInstance(moscowTz)
    val yesterday = Calendar.getInstance(moscowTz).apply { add(Calendar.DAY_OF_YEAR, -1) }

    val sameDay: (Calendar, Calendar) -> Boolean = { a, b ->
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

    return when {
        sameDay(calTs, today) -> "$todayPrefix, ${timeFormat.format(Date(timestamp))}"
        sameDay(calTs, yesterday) -> "$yesterdayPrefix, ${timeFormat.format(Date(timestamp))}"
        else -> SimpleDateFormat("d MMM, HH:mm", Locale("ru"))
            .apply { timeZone = moscowTz }
            .format(Date(timestamp))
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

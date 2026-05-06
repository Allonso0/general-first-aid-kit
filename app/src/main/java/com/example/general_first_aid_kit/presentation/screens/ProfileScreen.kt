package com.example.general_first_aid_kit.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.component.ProfileMenuItem
import com.example.general_first_aid_kit.presentation.ui.theme.KitColorBlueGray
import com.example.general_first_aid_kit.presentation.ui.theme.KitColorLavender
import com.example.general_first_aid_kit.presentation.ui.theme.KitColorOrange
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.LightGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.TextGreen
import com.example.general_first_aid_kit.presentation.ui.theme.KitColorRed
import com.example.general_first_aid_kit.presentation.ui.theme.TextRed
import com.example.general_first_aid_kit.presentation.ui.theme.White
import com.example.general_first_aid_kit.presentation.viewmodels.ProfileViewModel
import com.example.general_first_aid_kit.presentation.utils.shimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToProfileSettings: () -> Unit,
    onNavigateToAppSettings: () -> Unit,
    onNavigateToNotificationLog: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    var isImageLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.reloadUser()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    text = stringResource(R.string.profile),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextGreen,
                    textAlign = TextAlign.Center
                ) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = GreenPrimary
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = stringResource(R.string.back_button_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White
                )
            )
        },
        containerColor = White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(innerPadding)
                .padding(horizontal = Dimensions.PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

            Box(
                modifier = Modifier
                    .size(Dimensions.AvatarLarge)
                    .clip(CircleShape)
                    .background(LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (user?.avatarURL != null) {
                    AsyncImage(
                        model = user?.avatarURL,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (isImageLoading) Modifier.shimmerEffect() else Modifier),
                        contentScale = ContentScale.Crop,
                        onSuccess = { isImageLoading = false },
                        onError = { isImageLoading = false }
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.baseline_person_24),
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.AvatarMedium),
                        tint = LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            Text(
                text = user?.name ?: "Загрузка...",
                style = MaterialTheme.typography.titleMedium,
                color = TextBlack
            )

            Text(
                text = user?.email ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = TextBlack
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingExtraLarge))

            ProfileMenuItem(
                icon = painterResource(R.drawable.baseline_person_24),
                text = stringResource(R.string.profile_settings),
                iconContainerColor = GreenPrimary,
                onClick = { onNavigateToProfileSettings() }
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

            ProfileMenuItem(
                icon = painterResource(R.drawable.baseline_settings_24),
                text = stringResource(R.string.settings),
                iconContainerColor = KitColorBlueGray,
                onClick = { onNavigateToAppSettings() }
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

            ProfileMenuItem(
                icon = painterResource(R.drawable.baseline_notifications_24),
                text = stringResource(R.string.notifications_log),
                iconContainerColor = KitColorOrange,
                onClick = { onNavigateToNotificationLog() }
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

            ProfileMenuItem(
                icon = painterResource(R.drawable.baseline_question_answer_24),
                text = stringResource(R.string.how_to_use_the_app),
                iconContainerColor = KitColorLavender,
                onClick = { }
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingExtraLarge))

            ProfileMenuItem(
                icon = painterResource(R.drawable.baseline_logout_24),
                text = stringResource(R.string.log_out),
                iconContainerColor = KitColorRed,
                textColor = TextRed,
                onClick = { onLogout() }
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
        }

    }
}

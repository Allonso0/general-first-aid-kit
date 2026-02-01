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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.example.general_first_aid_kit.presentation.ui.theme.Black
import com.example.general_first_aid_kit.presentation.ui.theme.ButtonRed
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.LightGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.TextGreen
import com.example.general_first_aid_kit.presentation.ui.theme.TextRed
import com.example.general_first_aid_kit.presentation.ui.theme.White
import com.example.general_first_aid_kit.presentation.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToProfileSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()

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
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
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
            HorizontalDivider(color = Black)

            ProfileMenuItem(
                icon = painterResource(R.drawable.baseline_person_24),
                text = stringResource(R.string.profile_settings),
                onClick = { onNavigateToProfileSettings() }
            )

            ProfileMenuItem(
                icon = painterResource(R.drawable.baseline_settings_24),
                text = stringResource(R.string.settings),
                onClick = { }
            )

            ProfileMenuItem(
                icon = painterResource(R.drawable.baseline_notifications_24),
                text = stringResource(R.string.notifications_log),
                onClick = { }
            )

            ProfileMenuItem(
                icon = painterResource(R.drawable.baseline_question_answer_24),
                text = stringResource(R.string.how_to_use_the_app),
                onClick = { }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onLogout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.LargeButtonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonRed,
                    contentColor = TextRed
                )
            ) {
                Text(
                    text = stringResource(R.string.log_out)
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
        }

    }
}
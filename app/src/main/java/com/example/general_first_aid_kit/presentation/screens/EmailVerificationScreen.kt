package com.example.general_first_aid_kit.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.component.GreenSemiCircle
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.LightGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.TextGreen
import com.example.general_first_aid_kit.presentation.ui.theme.TextWhite
import com.example.general_first_aid_kit.presentation.ui.theme.White
import com.example.general_first_aid_kit.presentation.viewmodels.EmailVerificationState
import com.example.general_first_aid_kit.presentation.viewmodels.EmailVerificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(
    onVerified: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: EmailVerificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is EmailVerificationState.Verified) {
            onVerified()
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.email_verification_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextGreen,
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onSignOut,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = GreenPrimary)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = stringResource(R.string.back_button_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        containerColor = White,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            GreenSemiCircle(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
                    .align(Alignment.BottomCenter)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimensions.PaddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                Text(
                    text = stringResource(R.string.email_verification_greeting_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    color = TextBlack
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

                Text(
                    text = stringResource(R.string.email_verification_description),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    color = TextBlack
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingExtraLarge))

                Button(
                    onClick = { viewModel.onCheckClick() },
                    enabled = uiState !is EmailVerificationState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.MediumButtonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor = TextWhite,
                        disabledContainerColor = LightGray
                    )
                ) {
                    if (uiState is EmailVerificationState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimensions.MediumButtonHeight - Dimensions.PaddingMedium),
                            color = White
                        )
                    } else {
                        Text(text = stringResource(R.string.btn_email_confirmed))
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

                Text(
                    text = stringResource(R.string.btn_resend_email),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextGreen,
                    modifier = Modifier.clickable { viewModel.onResendClick() }
                )
            }

        }
    }
}

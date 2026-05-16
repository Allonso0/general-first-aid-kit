package com.example.general_first_aid_kit.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.component.GreenSemiCircle
import com.example.general_first_aid_kit.presentation.ui.theme.Black
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.LightGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.TextGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextGreen
import com.example.general_first_aid_kit.presentation.ui.theme.TextWhite
import com.example.general_first_aid_kit.presentation.ui.theme.White
import com.example.general_first_aid_kit.presentation.viewmodels.ForgotPasswordState
import com.example.general_first_aid_kit.presentation.viewmodels.ForgotPasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val emailError by viewModel.emailError.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }
    var emailTouched by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is ForgotPasswordState.Error) {
            snackbarHostState.showSnackbar(
                (uiState as ForgotPasswordState.Error).message,
                duration = SnackbarDuration.Short
            )
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.forgot_password_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextGreen,
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
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
            if (uiState is ForgotPasswordState.Success) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimensions.PaddingLarge),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.forgot_password_success_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextGreen,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                    Text(
                        text = stringResource(R.string.forgot_password_success_description),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextBlack,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimensions.PaddingLarge),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                    Text(
                        text = stringResource(R.string.forgot_password_greeting_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth(),
                        color = TextBlack
                    )

                    Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

                    Text(
                        text = stringResource(R.string.forgot_password_description),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        color = TextBlack
                    )

                    Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            viewModel.onEmailChange(it)
                        },
                        label = { Text(text = stringResource(R.string.email_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    emailTouched = true
                                } else if (emailTouched) {
                                    viewModel.onEmailChange(email)
                                }
                            },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        isError = emailError != null,
                        supportingText = emailError?.let { resId ->
                            { Text(stringResource(resId)) }
                        },
                        shape = RoundedCornerShape(Dimensions.CornerRadiusExtraLarge),
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = Black,
                            focusedBorderColor = GreenPrimary,
                            focusedLabelColor = GreenPrimary,
                            focusedTextColor = TextBlack,
                            unfocusedTextColor = TextGray,
                            unfocusedLabelColor = LightGray,
                            unfocusedBorderColor = LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(Dimensions.SpacingExtraLarge))

                    Button(
                        onClick = { viewModel.onSendClick(email) },
                        enabled = uiState !is ForgotPasswordState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimensions.MediumButtonHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary,
                            contentColor = TextWhite,
                            disabledContainerColor = LightGray
                        )
                    ) {
                        if (uiState is ForgotPasswordState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimensions.MediumButtonHeight - Dimensions.PaddingMedium),
                                color = White
                            )
                        } else {
                            Text(text = stringResource(R.string.btn_send_reset_link))
                        }
                    }
                }
            }

            GreenSemiCircle(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.SemiCircleHeightDefault)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

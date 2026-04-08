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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import com.example.general_first_aid_kit.presentation.viewmodels.AuthState
import com.example.general_first_aid_kit.presentation.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onNavigateBack: () -> Unit,
    onRegistrationClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthState.Authenticated -> {
                onRegistrationClick()
                viewModel.resetState()
            }
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (uiState as AuthState.Error).message,
                    duration = SnackbarDuration.Short
                )
                viewModel.resetState()
            }
            is AuthState.ErrorRes -> {
                // Обработка ошибки из ресурсов
            }
            else -> Unit
        }
    }

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    text = stringResource(R.string.registration_title),
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
                }
            )
        },
        containerColor = White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.PaddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                Text(
                    text = stringResource(R.string.registration_greeting_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

                Text(
                    text = stringResource(R.string.registration_description_title),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(
                        text = stringResource(R.string.username_label)
                    ) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    shape = RoundedCornerShape(Dimensions.CornerRadiusExtraLarge),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Black,

                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        focusedTextColor = TextBlack,
                        focusedTrailingIconColor = GreenPrimary,

                        unfocusedTextColor = TextGray,
                        unfocusedLabelColor = LightGray,
                        unfocusedBorderColor = LightGray,
                        unfocusedTrailingIconColor = LightGray
                    )
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(
                        text = stringResource(R.string.email_label)
                    ) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    shape = RoundedCornerShape(Dimensions.CornerRadiusExtraLarge),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Black,

                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        focusedTextColor = TextBlack,
                        focusedTrailingIconColor = GreenPrimary,

                        unfocusedTextColor = TextGray,
                        unfocusedLabelColor = LightGray,
                        unfocusedBorderColor = LightGray,
                        unfocusedTrailingIconColor = LightGray
                    )
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(
                        text = stringResource(R.string.password_label)
                    ) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(Dimensions.CornerRadiusExtraLarge),
                    trailingIcon = {
                        val image = if (isPasswordVisible) painterResource(R.drawable.baseline_visibility_off_24) else painterResource(R.drawable.baseline_visibility_24)
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                painter = image,
                                contentDescription = stringResource(R.string.visibility_switch)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Black,

                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        focusedTextColor = TextBlack,
                        focusedTrailingIconColor = GreenPrimary,

                        unfocusedTextColor = TextGray,
                        unfocusedLabelColor = LightGray,
                        unfocusedBorderColor = LightGray,
                        unfocusedTrailingIconColor = LightGray
                    )
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingExtraLarge))

                Button(
                    onClick = {
                        viewModel.onRegisterClick(email, password, username)
                    },
                    enabled = uiState !is AuthState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.MediumButtonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor = TextWhite,
                        disabledContainerColor = LightGray
                    )
                ) {
                    if (uiState is AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(Dimensions.MediumButtonHeight - Dimensions.PaddingMedium),
                            color = White
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.btn_create_account)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
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

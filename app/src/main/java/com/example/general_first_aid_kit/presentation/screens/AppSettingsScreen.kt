package com.example.general_first_aid_kit.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.component.GreenSemiCircle
import com.example.general_first_aid_kit.presentation.ui.theme.Black
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.LightGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.TextGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextGreen
import com.example.general_first_aid_kit.presentation.ui.theme.White
import com.example.general_first_aid_kit.presentation.viewmodels.AppSettingsEvent
import com.example.general_first_aid_kit.presentation.viewmodels.AppSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
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
            GreenSemiCircle(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
                    .align(Alignment.BottomCenter)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.PaddingLarge)
                    .verticalScroll(rememberScrollState())
            ) {
            Text(
                text = stringResource(R.string.settings_notifications_section),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                SettingsNumberField(
                    value = state.lowStockThreshold,
                    label = stringResource(R.string.low_stock_threshold_label),
                    onValueChange = { viewModel.onEvent(AppSettingsEvent.LowStockThresholdChanged(it)) },
                    onFocusLost = { viewModel.onEvent(AppSettingsEvent.LowStockThresholdCommitted) }
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

                Text(
                    text = stringResource(R.string.low_stock_threshold_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray,
                    modifier = Modifier.padding(horizontal = Dimensions.PaddingSmall)
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                SettingsNumberField(
                    value = state.expiryWarningDays,
                    label = stringResource(R.string.expiry_warning_days_label),
                    onValueChange = { viewModel.onEvent(AppSettingsEvent.ExpiryWarningDaysChanged(it)) },
                    onFocusLost = { viewModel.onEvent(AppSettingsEvent.ExpiryWarningDaysCommitted) }
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

                Text(
                    text = stringResource(R.string.expiry_warning_days_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray,
                    modifier = Modifier.padding(horizontal = Dimensions.PaddingSmall)
                )
            }
        }
        }
    }
}

@Composable
private fun SettingsNumberField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    onFocusLost: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= 3) onValueChange(it) },
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { if (!it.isFocused) onFocusLost() },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
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
}

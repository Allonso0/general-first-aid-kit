package com.example.general_first_aid_kit.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.component.ColorSelectionDialog
import com.example.general_first_aid_kit.presentation.component.KitTypeRadioButton
import com.example.general_first_aid_kit.presentation.ui.theme.Black
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.KitColors
import com.example.general_first_aid_kit.presentation.ui.theme.LightGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.TextGray
import com.example.general_first_aid_kit.presentation.ui.theme.White
import com.example.general_first_aid_kit.presentation.viewmodels.CreateKitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateKitScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateKitViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showColorDialog by remember { mutableStateOf(false) }

    if (showColorDialog) {
        ColorSelectionDialog(
            onDismiss = { showColorDialog = false },
            onColorSelected = { index ->
                viewModel.onColorSelected(index)
                showColorDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.new_kit),
                        style = MaterialTheme.typography.titleLarge,
                        color = White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.createKit(onSuccess = { onNavigateBack() })
                        },
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = White, modifier = Modifier.size(
                                Dimensions.SmallButtonHeight))
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.baseline_check_24),
                                contentDescription = stringResource(R.string.save),
                                tint = White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary
                )
            )
        },
        containerColor = GreenPrimary
    ) { innerPadding ->

        val topOffset = innerPadding.calculateTopPadding() + Dimensions.PaddingLarge

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topOffset),
            color = White,
            shape = RoundedCornerShape(
                topStart = Dimensions.CornerRadiusExtraLarge,
                topEnd = Dimensions.CornerRadiusExtraLarge
            )
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(Dimensions.PaddingLarge)
                    .padding(bottom = innerPadding.calculateBottomPadding()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.disclaimer),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
                Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = stringResource(R.string.cover_color),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )
                    Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimensions.LargeButtonHeight)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    color = KitColors.getOrElse(state.colorIndex) { LightGray },
                                    shape = RoundedCornerShape(
                                        topStart = Dimensions.CornerRadiusMedium,
                                        bottomStart = Dimensions.CornerRadiusMedium,
                                        topEnd = 0.dp,
                                        bottomEnd = 0.dp
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.width(Dimensions.PaddingSmall))

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(Dimensions.LargeButtonWidth)
                                .background(
                                    color = GreenPrimary,
                                    shape = RoundedCornerShape(
                                        topStart = 0.dp,
                                        bottomStart = 0.dp,
                                        topEnd = Dimensions.CornerRadiusMedium,
                                        bottomEnd = Dimensions.CornerRadiusMedium
                                    )
                                )
                                .clickable { showColorDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.choose),
                                color = White,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimensions.PaddingExtraLarge))

                    Text(
                        text = stringResource(R.string.main_info),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )
                    Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text(stringResource(R.string.kit_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
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
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

                    OutlinedTextField(
                        value = state.location,
                        onValueChange = { viewModel.onLocationChange(it) },
                        label = { Text(stringResource(R.string.location)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
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
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(Dimensions.PaddingExtraLarge))

                    Text(
                        text = stringResource(R.string.kit_type),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )
                    Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

                    KitTypeRadioButton(
                        selected = !state.isShared,
                        title = stringResource(R.string.personal_kit),
                        subtitle = stringResource(R.string.personal_kit_description),
                        onClick = { viewModel.onTypeChange(false) }
                    )

                    KitTypeRadioButton(
                        selected = state.isShared,
                        title = stringResource(R.string.public_kit),
                        subtitle = stringResource(R.string.public_kit_description),
                        onClick = { viewModel.onTypeChange(true) }
                    )

                    Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
                }
            }
        }
    }
}
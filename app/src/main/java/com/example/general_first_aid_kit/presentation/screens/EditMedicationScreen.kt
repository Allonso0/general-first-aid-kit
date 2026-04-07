package com.example.general_first_aid_kit.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.component.KitInputField
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.LightGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.TextGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextRed
import com.example.general_first_aid_kit.presentation.ui.theme.White
import com.example.general_first_aid_kit.presentation.utils.shimmerEffect
import com.example.general_first_aid_kit.presentation.viewmodels.EditMedicationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicationScreen(
    kitId: String,
    medicationId: String,
    onNavigateBack: () -> Unit,
    onDeleteSuccess: () -> Unit,
    viewModel: EditMedicationViewModel = hiltViewModel()
) {
    LaunchedEffect(kitId, medicationId) {
        viewModel.loadMedication(kitId, medicationId)
        viewModel.startObservingKit(kitId)
    }

    val isKicked by viewModel.isUserKickedOrDeleted.collectAsState()
    LaunchedEffect(isKicked) {
        if (isKicked) {
            onDeleteSuccess()
        }
    }

    val state by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.expirationDateMillis ?: System.currentTimeMillis()
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.updatePhotoUri(uri?.toString()) }
    )

    var expanded by remember { mutableStateOf(false) }
    val categories = listOf(
        stringResource(R.string.no_category),
        stringResource(R.string.cat_antipyretic),
        stringResource(R.string.cat_painkiller),
        stringResource(R.string.cat_antihistamine),
        stringResource(R.string.cat_spasmolytic),
        stringResource(R.string.cat_antibiotic),
        stringResource(R.string.cat_vitamins),
        stringResource(R.string.cat_antiseptic)
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить лекарство?") },
            text = { Text("Вы уверены, что хотите удалить это лекарство? Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMedication(onSuccess = onDeleteSuccess)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = TextRed)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена", color = TextGray)
                }
            },
            containerColor = White
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateExpirationDate(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.choose), color = GreenPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel), color = TextGray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Редактирование", style = MaterialTheme.typography.titleLarge, color = GreenPrimary, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painterResource(R.drawable.baseline_arrow_back_ios_24), contentDescription = stringResource(R.string.back_button_description), tint = GreenPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveMedication(onSuccess = onNavigateBack) },
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimensions.SmallButtonHeight),
                                color = GreenPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(painterResource(R.drawable.baseline_check_24), contentDescription = stringResource(R.string.save), tint = GreenPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimensions.PaddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
        ) {
            if (state.error != null) {
                Text(text = state.error!!, color = TextRed, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth())
            }

            Box(
                modifier = Modifier
                    .size(Dimensions.MedicationPhotoSize)
                    .clip(RoundedCornerShape(Dimensions.CornerRadiusMedium))
                    .background(LightGray.copy(alpha = 0.3f))
                    .clickable {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (state.photoUri != null) {
                    AsyncImage(
                        model = state.photoUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().shimmerEffect(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(painterResource(R.drawable.baseline_add_a_photo_24), contentDescription = null, tint = TextGray, modifier = Modifier.size(32.dp))
                        Text(stringResource(R.string.add_photo), style = MaterialTheme.typography.labelSmall, color = TextGray)
                    }
                }
            }

            KitInputField(
                value = state.name,
                onValueChange = viewModel::updateName,
                label = stringResource(R.string.medication_name_label),
                error = state.nameErrorResId?.let { stringResource(it) },
                focusedTextColor = TextBlack,
                unfocusedTextColor = TextGray
            )

            Box(modifier = Modifier.clickable { showDatePicker = true }) {
                OutlinedTextField(
                    value = state.expirationDateMillis?.let { formatDate(it) } ?: "",
                    onValueChange = {},
                    label = { Text(stringResource(R.string.expiration_date_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    readOnly = true,
                    isError = state.expirationDateErrorResId != null,
                    shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = TextGray,
                        disabledBorderColor = if (state.expirationDateErrorResId != null) MaterialTheme.colorScheme.error else if (state.expirationDateMillis != null) GreenPrimary else LightGray,
                        disabledLabelColor = if (state.expirationDateErrorResId != null) MaterialTheme.colorScheme.error else if (state.expirationDateMillis != null) GreenPrimary else TextGray,
                        disabledTrailingIconColor = if (state.expirationDateMillis != null) GreenPrimary else TextGray
                    ),
                    trailingIcon = { Icon(painterResource(R.drawable.baseline_calendar_today_24), contentDescription = null) }
                )
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.category.ifEmpty { stringResource(R.string.no_category) },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.category_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        unfocusedBorderColor = LightGray,
                        unfocusedLabelColor = TextGray,
                        focusedTextColor = TextBlack,
                        unfocusedTextColor = TextGray
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(White)
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(text = category, color = if (expanded) TextBlack else TextGray) },
                            onClick = {
                                viewModel.updateCategory(category)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
            ) {
                KitInputField(
                    value = state.quantity,
                    onValueChange = viewModel::updateQuantity,
                    label = stringResource(R.string.quantity_label),
                    modifier = Modifier.weight(1f),
                    error = state.quantityErrorResId?.let { stringResource(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    focusedTextColor = TextBlack,
                    unfocusedTextColor = TextGray
                )

                OutlinedTextField(
                    value = state.unit,
                    onValueChange = viewModel::updateUnit,
                    label = { Text(stringResource(R.string.unit_label)) },
                    modifier = Modifier.width(100.dp),
                    shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        unfocusedBorderColor = LightGray,
                        unfocusedLabelColor = TextGray,
                        unfocusedTextColor = TextGray,
                        focusedTextColor = TextBlack
                    )
                )
            }

            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::updateDescription,
                label = { Text(stringResource(R.string.description_label)) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    focusedLabelColor = GreenPrimary,
                    unfocusedBorderColor = LightGray,
                    unfocusedLabelColor = TextGray,
                    unfocusedTextColor = TextGray,
                    focusedTextColor = TextBlack
                )
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().height(Dimensions.MediumButtonHeight),
                colors = ButtonDefaults.buttonColors(containerColor = White),
                border = androidx.compose.foundation.BorderStroke(1.dp, TextRed),
                shape = RoundedCornerShape(Dimensions.CornerRadiusMedium)
            ) {
                Text("Удалить лекарство", color = TextRed)
            }
        }
    }
}

private fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}
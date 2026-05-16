package com.example.general_first_aid_kit.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.component.GreenSemiCircle
import com.example.general_first_aid_kit.presentation.component.OfflineBanner
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.LightGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.TextGray
import com.example.general_first_aid_kit.presentation.ui.theme.White
import com.example.general_first_aid_kit.presentation.utils.formatExpirationDate
import com.example.general_first_aid_kit.presentation.utils.getCategoryColor
import com.example.general_first_aid_kit.presentation.utils.shimmerEffect
import com.example.general_first_aid_kit.presentation.viewmodels.MedicationInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationInfoScreen(
    kitId: String,
    medicationId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String, String) -> Unit,
    viewModel: MedicationInfoViewModel = hiltViewModel()
) {
    LaunchedEffect(kitId, medicationId) {
        viewModel.loadMedication(kitId, medicationId)
        viewModel.startObservingKit(kitId)
    }

    val isKicked by viewModel.isUserKickedOrDeleted.collectAsState()
    LaunchedEffect(isKicked) {
        if (isKicked) {
            onNavigateBack()
        }
    }


    val medication by viewModel.medication.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isKitShared by viewModel.isKitShared.collectAsState()
    val canModify = !(isKitShared && !isOnline)

    Scaffold(
        containerColor = White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "О лекарстве",
                        style = MaterialTheme.typography.titleLarge,
                        color = GreenPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = "Назад",
                            tint = GreenPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigateToEdit(kitId, medicationId) },
                        enabled = canModify
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_edit_note_24),
                            contentDescription = "Редактировать",
                            tint = if (canModify) GreenPrimary else LightGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            if (!canModify) {
                OfflineBanner(stringResource(R.string.offline_banner_read_only))
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {

            GreenSemiCircle(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .align(Alignment.BottomCenter)
            )

            medication?.let { med ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(Dimensions.PaddingMedium),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .size(Dimensions.MedicationPhotoSize)
                            .border(
                                width = 1.dp,
                                color = GreenPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(Dimensions.CornerRadiusMedium)
                            ),
                        shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
                        color = White,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(LightGray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!med.photoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = med.photoUrl,
                                    contentDescription = med.name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .shimmerEffect(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_medication_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = TextGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                    Text(
                        text = med.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextBlack,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))

                    val categoryColor = getCategoryColor(med.category)
                    Box(
                        modifier = Modifier
                            .border(1.dp, categoryColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = med.category.ifEmpty { "Без категории" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = categoryColor,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
                    ) {
                        InfoCard(
                            modifier = Modifier.weight(1f),
                            label = "Срок годности",
                            value = formatExpirationDate(med.expirationDate),
                            iconRes = R.drawable.baseline_calendar_today_24
                        )
                        InfoCard(
                            modifier = Modifier.weight(1f),
                            label = "В наличии",
                            value = "${med.quantity} ${med.unit}",
                            iconRes = R.drawable.baseline_medication_24
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = GreenPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(Dimensions.CornerRadiusMedium)
                            ),
                        shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
                        color = White.copy(alpha = 0.85f),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.PaddingMedium),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Изменить количество",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                QuantityButton(
                                    iconRes = R.drawable.baseline_remove_24,
                                    onClick = { viewModel.updateQuantity(-1) },
                                    enabled = canModify
                                )
                                Text(
                                    text = med.quantity.toString(),
                                    modifier = Modifier.padding(horizontal = Dimensions.PaddingExtraLarge),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextBlack
                                )
                                QuantityButton(
                                    iconRes = R.drawable.baseline_add_24,
                                    onClick = { viewModel.updateQuantity(1) },
                                    enabled = canModify
                                )
                            }
                        }
                    }


                    if (med.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = GreenPrimary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(Dimensions.CornerRadiusMedium)
                                ),
                            shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
                            color = White.copy(alpha = 0.85f),
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimensions.PaddingMedium)
                            ) {
                                Text(
                                    text = "Описание",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextBlack
                                )
                                Spacer(modifier = Modifier.height(Dimensions.SpacingSmall))
                                Text(
                                    text = med.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextGray,
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(Dimensions.SpacingExtraLarge))
                }
            } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
            }
        }
    }
}

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    iconRes: Int
) {
    Surface(
        modifier = modifier
            .border(
                width = 1.dp,
                color = GreenPrimary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(Dimensions.CornerRadiusMedium)
            ),
        shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
        color = White.copy(alpha = 0.85f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(Dimensions.PaddingMedium)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(Dimensions.SpacingExtraSmall))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextGray,
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextBlack,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun QuantityButton(iconRes: Int, onClick: () -> Unit, enabled: Boolean = true) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(48.dp)
            .background(
                if (enabled) GreenPrimary.copy(alpha = 0.1f) else LightGray.copy(alpha = 0.1f),
                CircleShape
            )
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (enabled) GreenPrimary else LightGray
        )
    }
}
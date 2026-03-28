package com.example.general_first_aid_kit.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.component.ExpandableAddMedicationFAB
import com.example.general_first_aid_kit.presentation.component.MedicationCard
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.LightGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.TextGray
import com.example.general_first_aid_kit.presentation.ui.theme.White
import com.example.general_first_aid_kit.presentation.viewmodels.KitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitScreen(
    kitId: String,
    kitName: String,
    onNavigateBack: () -> Unit,
    onNavigateToKitSettings: () -> Unit,
    onNavigateToAddManual: (String) -> Unit,
    viewModel: KitViewModel = hiltViewModel()
) {
    LaunchedEffect(kitId) {
        viewModel.loadKit(kitId)
    }

    val medications by viewModel.medications.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showFilterMenu by remember { mutableStateOf(false) }
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categories = listOf(
        "Все",
        "Без категории",
        "Жаропонижающее",
        "Обезболивающее",
        "Антигистаминное",
        "Спазмолитик",
        "Антибиотик",
        "Витамины",
        "Антисептик"
    )

    Scaffold(
        containerColor = White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = kitName,
                        style = MaterialTheme.typography.titleLarge,
                        color = GreenPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = GreenPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToKitSettings) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_more_horiz_24),
                            contentDescription = "Инфо",
                            tint = GreenPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White
                )
            )
        },
        floatingActionButton = {
            ExpandableAddMedicationFAB(
                onAddManual = { onNavigateToAddManual(kitId) },
                onScanBarcode = {
                    // Заглушка
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.PaddingMedium, vertical = Dimensions.PaddingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Введите название лекарства...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextGray
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimensions.SearchFieldHeight),
                    shape = RoundedCornerShape(Dimensions.CornerRadiusLarge),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = TextGray,
                        unfocusedTrailingIconColor = LightGray,
                        focusedBorderColor = GreenPrimary,
                        focusedTrailingIconColor = GreenPrimary,
                    ),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.baseline_search_24),
                            contentDescription = null
                        )
                    }
                )

                Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))

                Box {
                    Box(
                        modifier = Modifier
                            .size(Dimensions.SearchFieldHeight)
                            .clip(CircleShape)
                            .background(GreenPrimary)
                            .clickable { showFilterMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_filter_list_24),
                            contentDescription = "Фильтр",
                            tint = White
                        )
                    }

                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false },
                        offset = DpOffset(x = 0.dp, y = Dimensions.SpacingSmall),
                        shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
                        modifier = Modifier
                            .background(White, RoundedCornerShape(Dimensions.CornerRadiusMedium))
                            .border(1.dp, LightGray.copy(0.5f), RoundedCornerShape(Dimensions.CornerRadiusMedium))
                    ) {
                        categories.forEach { category ->
                            val isSelected = category == selectedCategory

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = category,
                                        color = TextBlack,
                                        fontWeight = FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.onCategorySelected(category)
                                    showFilterMenu = false
                                },
                                modifier = Modifier.background(
                                    if (isSelected) GreenPrimary.copy(alpha = 0.1f) else White
                                ),
                                trailingIcon = {
                                    if (isSelected) {
                                        Icon(
                                            painter = painterResource(R.drawable.baseline_check_24),
                                            contentDescription = "Выбрано",
                                            tint = GreenPrimary
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (medications.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("В этой аптечке пока пусто", color = TextGray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(Dimensions.PaddingMedium),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
                ) {
                    items(medications, key = { it.id }) { med ->
                        MedicationCard(
                            medication = med,
                            onClick = { /* Переход к деталям позже */ }
                        )
                    }
                }
            }

        }
    }
}
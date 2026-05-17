package com.example.general_first_aid_kit.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.component.GenericTabRow
import com.example.general_first_aid_kit.presentation.component.KitCard
import com.example.general_first_aid_kit.presentation.component.OfflineBanner
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.TextGreen
import com.example.general_first_aid_kit.presentation.ui.theme.TextRed
import com.example.general_first_aid_kit.presentation.ui.theme.White
import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.presentation.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onProfileClick: () -> Unit,
    onJoinClick: () -> Unit,
    onAddKitClick: () -> Unit,
    onKitCardClick: (String, String, String, Int, Boolean) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isArchiveMode by viewModel.isArchiveMode.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    val tabActive = stringResource(R.string.tab_active)
    val tabArchived = stringResource(R.string.tab_archived)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.kits),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextGreen
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onProfileClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = GreenPrimary
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_person_24),
                            contentDescription = stringResource(R.string.profile),
                            tint = White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onJoinClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = GreenPrimary
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_group_24),
                            contentDescription = stringResource(R.string.profile),
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White
                )
            )
        },
        floatingActionButton = {
            if (!isArchiveMode) {
                FloatingActionButton(
                    onClick = onAddKitClick,
                    shape = CircleShape,
                    containerColor = GreenPrimary,
                    contentColor = White
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_add_24),
                        contentDescription = stringResource(R.string.add_kit),
                        tint = White
                    )
                }
            }
        },
        containerColor = White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!isOnline) {
                OfflineBanner(stringResource(R.string.offline_no_internet))
            }

            GenericTabRow(
                selectedTabIndex = if (isArchiveMode) 1 else 0,
                tabs = listOf(tabActive, tabArchived),
                onTabSelected = { viewModel.setArchiveMode(it == 1) }
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = White
            ) {
                when {
                    state.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = GreenPrimary)
                        }
                    }

                    state.error != null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.error_with_detail, state.error ?: ""),
                                color = TextRed,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(Dimensions.PaddingMedium)
                            )
                        }
                    }

                    state.kits.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.kits_empty_hint),
                                color = GreenPrimary,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(Dimensions.PaddingExtraLarge)
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(Dimensions.PaddingMedium),
                            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
                        ) {
                            items(
                                items = state.kits,
                                key = { it.id }
                            ) { kit ->


                                /* val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { dismissValue ->
                                        when (dismissValue) {
                                            SwipeToDismissBoxValue.EndToStart -> {
                                                viewModel.deleteKit(kit)
                                                true
                                            }

                                            else -> false
                                        }
                                    },
                                    positionalThreshold = { it * 0.25f }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = false,
                                    backgroundContent = { DeleteBackground(dismissState) },
                                    content = {
                                        KitCard(
                                            kit = kit,
                                            onClick = { onKitCardClick(kit.id, kit.name, kit.location, kit.colorIndex, kit.type == KitType.SHARED) }
                                        )
                                    }
                                ) */

                                KitCard(
                                    kit = kit,
                                    onClick = { onKitCardClick(kit.id, kit.name, kit.location, kit.colorIndex, kit.type == KitType.SHARED) }
                                )

                            }
                        }
                    }
                }
            }
        }
    }
}
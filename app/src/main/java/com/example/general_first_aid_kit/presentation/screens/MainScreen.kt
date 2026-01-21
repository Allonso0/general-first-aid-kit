package com.example.general_first_aid_kit.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.TextGreen
import com.example.general_first_aid_kit.presentation.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onProfileClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    text = stringResource(R.string.kits),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextGreen
                ) },
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
                        onClick = { },
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
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
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
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Здесь будет список аптечек")
        }
    }
}
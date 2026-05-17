package com.example.general_first_aid_kit.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.White

@Composable
fun ExpandableAddMedicationFAB(
    onAddManual: () -> Unit,
    onScanBarcode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
            ) {
                FabSubItem(
                    label = stringResource(R.string.fab_scan_label),
                    iconRes = R.drawable.baseline_camera_alt_24,
                    onClick = {
                        expanded = false
                        onScanBarcode()
                    }
                )

                FabSubItem(
                    label = stringResource(R.string.fab_manual_label),
                    iconRes = R.drawable.baseline_edit_note_24,
                    onClick = {
                        expanded = false
                        onAddManual()
                    }
                )
            }
        }

        FloatingActionButton(
            onClick = { expanded = !expanded },
            shape = CircleShape,
            containerColor = GreenPrimary,
            contentColor = White,
            modifier = Modifier.size(Dimensions.LargeButtonHeight)
        ) {
            Icon(
                painter = if (expanded) painterResource(R.drawable.baseline_close_24) else painterResource(R.drawable.baseline_add_24),
                contentDescription = if (expanded) stringResource(R.string.onboarding_close) else stringResource(R.string.fab_add_medication_desc),
                tint = White
            )
        }
    }
}

@Composable
private fun FabSubItem(
    label: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = Dimensions.PaddingExtraSmall)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = GreenPrimary,
            modifier = Modifier.padding(end = Dimensions.PaddingMedium)
        )
        SmallFloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = White,
            contentColor = GreenPrimary,
            modifier = Modifier.size(Dimensions.MediumButtonHeight)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(Dimensions.SmallButtonHeight)
            )
        }
    }
}
package com.example.general_first_aid_kit.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.KitColors
import com.example.general_first_aid_kit.presentation.ui.theme.White

@Composable
fun ColorSelectionDialog(
    onDismiss: () -> Unit,
    onColorSelected: (Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(Dimensions.CornerRadiusLarge),
            color = White,
            modifier = Modifier.padding(Dimensions.PaddingMedium)
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.PaddingMedium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.choose_color),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = Dimensions.PaddingMedium)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
                ) {
                    itemsIndexed(KitColors) { index, color ->
                        Box(
                            modifier = Modifier
                                .size(Dimensions.MediumButtonHeight)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { onColorSelected(index) }
                        )
                    }
                }
            }
        }
    }
}
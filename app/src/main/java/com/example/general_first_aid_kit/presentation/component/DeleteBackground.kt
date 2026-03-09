package com.example.general_first_aid_kit.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.White

@Composable
fun DeleteBackground(
    swipeDismissState: SwipeToDismissBoxState
) {
    val color = if (swipeDismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
        Color.Red.copy(alpha = 0.8f)
    } else {
        Color.Transparent
    }

    Box (
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = Dimensions.PaddingMedium)
            .clip(RoundedCornerShape(Dimensions.CornerRadiusMedium))
            .background(color)
            .padding(horizontal = Dimensions.PaddingLarge),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_delete_24),
            contentDescription = stringResource(R.string.delete),
            tint = White
        )
    }
}
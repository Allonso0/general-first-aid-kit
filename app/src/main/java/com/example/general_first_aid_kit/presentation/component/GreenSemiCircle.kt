package com.example.general_first_aid_kit.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary

@Composable
fun GreenSemiCircle(
    modifier: Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        drawArc(
            color = GreenPrimary,
            startAngle = 0f,
            sweepAngle = -180f,
            useCenter = true,
            topLeft = Offset(x = -size.width * 0.25f, y = size.height * 0.5f),
            size = Size(width = size.width * 1.5f, height = size.height * 1.5f)
        )
    }
}
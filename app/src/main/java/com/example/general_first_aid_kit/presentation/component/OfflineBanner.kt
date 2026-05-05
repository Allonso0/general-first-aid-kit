package com.example.general_first_aid_kit.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.TextRed

@Composable
fun OfflineBanner(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.labelMedium,
        color = TextRed,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(TextRed.copy(alpha = 0.10f))
            .padding(horizontal = Dimensions.PaddingMedium, vertical = Dimensions.SpacingSmall)
    )
}

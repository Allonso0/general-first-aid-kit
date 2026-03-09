package com.example.general_first_aid_kit.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.TextGray
import com.example.general_first_aid_kit.presentation.ui.theme.White

@Composable
fun MedicationCard(
    medication: Medication,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimensions.MedicationCardHeight)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.MedicationCardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.PaddingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(Dimensions.CornerRadiusSmall))
                    .background(TextGray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {  }

            Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .border(1.dp, GreenPrimary, RoundedCornerShape(8.dp))
                        .padding(
                            horizontal = Dimensions.PaddingSmall,
                            vertical = Dimensions.CategoryVerticalPadding)
                ) {
                    Text(
                        text = medication.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = GreenPrimary
                    )
                }

                Spacer(modifier = Modifier.height(Dimensions.SpacingExtraSmall))

                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextBlack
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingExtraSmall))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(GreenPrimary))
                    Spacer(modifier = Modifier.width(Dimensions.SpacingExtraSmall))
                    Text(
                        text = "${medication.expirationDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextBlack
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(GreenPrimary))
                    Spacer(modifier = Modifier.width(Dimensions.SpacingExtraSmall))
                    Text(
                        text = "${medication.count} ${medication.measureUnit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextBlack
                    )
                }
            }
        }
    }

}
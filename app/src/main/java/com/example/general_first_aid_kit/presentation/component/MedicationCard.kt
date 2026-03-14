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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.TextGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextRed
import com.example.general_first_aid_kit.presentation.ui.theme.White
import com.example.general_first_aid_kit.presentation.utils.formatExpirationDate
import com.example.general_first_aid_kit.presentation.utils.getCategoryColor

@Composable
fun MedicationCard(
    medication: Medication,
    onClick: () -> Unit
) {
    val categoryName = medication.category.ifEmpty { "Без категории" }
    val categoryColor = getCategoryColor(categoryName)

    val formattedDate = formatExpirationDate(medication.expirationDate)

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
            ) {
                if (!medication.photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = medication.photoUrl,
                        contentDescription = "Фото ${medication.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.baseline_medication_24),
                        contentDescription = null,
                        tint = TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.width(Dimensions.SpacingSmall))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .border(1.dp, categoryColor, RoundedCornerShape(8.dp))
                        .padding(
                            horizontal = Dimensions.PaddingSmall,
                            vertical = Dimensions.CategoryVerticalPadding)
                ) {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = categoryColor
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
                    val isExpired = medication.expirationDate in 1 until System.currentTimeMillis()
                    val dateColor = if (isExpired) TextRed else GreenPrimary

                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(dateColor))
                    Spacer(modifier = Modifier.width(Dimensions.SpacingExtraSmall))
                    Text(
                        text = "Годен до: $formattedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextBlack
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(GreenPrimary))
                    Spacer(modifier = Modifier.width(Dimensions.SpacingExtraSmall))
                    Text(
                        text = "${medication.quantity} ${medication.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextBlack
                    )
                }
            }
        }
    }

}
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
import com.example.general_first_aid_kit.presentation.utils.shimmerEffect

private fun buildHighlightedName(name: String, query: String): androidx.compose.ui.text.AnnotatedString {
    if (query.isBlank()) return buildAnnotatedString { append(name) }
    val q = query.trim().lowercase()
    val index = name.lowercase().indexOf(q)
    if (index < 0) return buildAnnotatedString { append(name) }
    return buildAnnotatedString {
        append(name.substring(0, index))
        withStyle(SpanStyle(background = GreenPrimary.copy(alpha = 0.2f), fontWeight = FontWeight.SemiBold)) {
            append(name.substring(index, index + q.length))
        }
        append(name.substring(index + q.length))
    }
}

@Composable
fun MedicationCard(
    medication: Medication,
    searchQuery: String = "",
    onClick: () -> Unit
) {
    val categoryName = medication.category.ifEmpty { "Без категории" }
    val categoryColor = getCategoryColor(categoryName)

    val formattedDate = formatExpirationDate(medication.expirationDate)
    var isImageLoading by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimensions.MedicationCardHeight)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.MedicationCardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (isImageLoading) Modifier.shimmerEffect() else Modifier),
                        contentScale = ContentScale.Crop,
                        onSuccess = { isImageLoading = false },
                        onError = { isImageLoading = false }
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
                    text = buildHighlightedName(medication.name, searchQuery),
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

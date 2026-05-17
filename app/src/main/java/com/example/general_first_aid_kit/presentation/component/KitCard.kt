package com.example.general_first_aid_kit.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.presentation.ui.theme.Black
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.KitColors
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.White

@Composable
fun KitCard(
    kit: Kit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimensions.KitCardHeight)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.KitCardElevation)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).heightIn(min = Dimensions.KitCardHeight)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(Dimensions.KitCoverWidth)
                    .background(
                        color = KitColors[kit.colorIndex],
                        shape = RoundedCornerShape(
                            topStart = Dimensions.CornerRadiusMedium,
                            bottomStart = Dimensions.CornerRadiusMedium
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .padding(Dimensions.PaddingMedium)
                    .fillMaxWidth()
            ) {
                Text(
                    text = kit.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextBlack
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingExtraSmall))

                val typeText = if (kit.type == KitType.PERSONAL)
                    stringResource(R.string.kit_type_personal_short)
                else
                    stringResource(R.string.participants_count_short, kit.userIds.size)
                Text(
                    text = "$typeText • ${kit.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextBlack
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = Dimensions.PaddingSmall),
                    thickness = Dimensions.HorizontalDividerThickness,
                    color = Black
                )

                Column(verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingExtraSmall)) {
                    MedicineRow(
                        label = stringResource(R.string.label_medications_colon),
                        count = kit.countMedicine
                    )
                    MedicineRow(
                        label = stringResource(R.string.label_expired_colon),
                        count = kit.countExpired,
                    )
                    MedicineRow(
                        label = stringResource(R.string.label_running_out_colon),
                        count = kit.countRunningOut
                    )
                }
            }
        }
    }
}
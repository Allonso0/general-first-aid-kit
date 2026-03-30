package com.example.general_first_aid_kit.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.general_first_aid_kit.presentation.ui.theme.Black
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.KitColors
import com.example.general_first_aid_kit.presentation.ui.theme.LightGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.TextGray
import com.example.general_first_aid_kit.presentation.ui.theme.White

@Composable
fun KitSectionTitle(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = TextBlack,
        modifier = Modifier.padding(vertical = Dimensions.PaddingSmall)
    )
}

@Composable
fun KitInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    focusedTextColor: Color = TextBlack,
    unfocusedTextColor: Color = TextBlack
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimensions.CornerRadiusMedium),
            isError = error != null,
            keyboardOptions = keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = Black,
                focusedBorderColor = GreenPrimary,
                focusedLabelColor = GreenPrimary,
                focusedTextColor = focusedTextColor,
                unfocusedTextColor = unfocusedTextColor,
                unfocusedBorderColor = LightGray,
                unfocusedLabelColor = TextGray,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                errorSupportingTextColor = MaterialTheme.colorScheme.error
            ),
            singleLine = singleLine,
            supportingText = if (error != null) {
                { Text(text = error) }
            } else null
        )
    }
}

@Composable
fun KitColorPreview(
    colorIndex: Int,
    onChooseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimensions.LargeButtonHeight)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    color = KitColors.getOrElse(colorIndex) { LightGray },
                    shape = RoundedCornerShape(
                        topStart = Dimensions.CornerRadiusMedium,
                        bottomStart = Dimensions.CornerRadiusMedium
                    )
                )
        )
        Spacer(modifier = Modifier.width(Dimensions.PaddingSmall))
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(Dimensions.LargeButtonWidth)
                .background(
                    color = GreenPrimary,
                    shape = RoundedCornerShape(
                        topEnd = Dimensions.CornerRadiusMedium,
                        bottomEnd = Dimensions.CornerRadiusMedium
                    )
                )
                .clickable { onChooseClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Выбрать", color = White, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun GenericTabRow(
    selectedTabIndex: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit
) {
    SecondaryTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = White,
        contentColor = GreenPrimary,
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                color = GreenPrimary,
                height = 3.dp
            )
        },
        divider = { }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                selectedContentColor = GreenPrimary,
                unselectedContentColor = TextGray
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(Dimensions.PaddingMedium),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
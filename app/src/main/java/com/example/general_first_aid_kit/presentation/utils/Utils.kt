package com.example.general_first_aid_kit.presentation.utils

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatExpirationDate(millis: Long): String {
    if (millis == 0L) return "Срок не указан"
    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

fun getCategoryColor(category: String): Color {
    return when (category.lowercase(Locale.getDefault())) {
        "жаропонижающее" -> Color(0xFFE57373)   // Красный
        "обезболивающее" -> Color(0xFFBA68C8)   // Фиолетовый
        "антигистаминное" -> Color(0xFF64B5F6)  // Синий
        "спазмолитик" -> Color(0xFF4DB6AC)      // Бирюзовый
        "антибиотик" -> Color(0xFFFFB74D)       // Оранжевый
        "витамины" -> Color(0xFFAED581)         // Салатовый
        "антисептик" -> Color(0xFF4DD0E1)       // Голубой
        "без категории" -> Color(0xFF9E9E9E)    // Серый (TextGray)
        else -> Color(0xFF4CAF50)
    }
}
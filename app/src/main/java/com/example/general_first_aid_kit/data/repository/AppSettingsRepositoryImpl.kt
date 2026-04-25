package com.example.general_first_aid_kit.data.repository

import android.content.SharedPreferences
import com.example.general_first_aid_kit.domain.model.AppSettings
import com.example.general_first_aid_kit.domain.repository.AppSettingsRepository
import javax.inject.Inject

private const val KEY_LOW_STOCK_THRESHOLD = "low_stock_threshold"
private const val KEY_EXPIRY_WARNING_DAYS = "expiry_warning_days"

class AppSettingsRepositoryImpl @Inject constructor(
    private val prefs: SharedPreferences
) : AppSettingsRepository {

    override fun getSettings(): AppSettings = AppSettings(
        lowStockThreshold = prefs.getInt(KEY_LOW_STOCK_THRESHOLD, AppSettings().lowStockThreshold),
        expiryWarningDays = prefs.getInt(KEY_EXPIRY_WARNING_DAYS, AppSettings().expiryWarningDays)
    )

    override fun saveSettings(settings: AppSettings) {
        prefs.edit()
            .putInt(KEY_LOW_STOCK_THRESHOLD, settings.lowStockThreshold)
            .putInt(KEY_EXPIRY_WARNING_DAYS, settings.expiryWarningDays)
            .apply()
    }
}

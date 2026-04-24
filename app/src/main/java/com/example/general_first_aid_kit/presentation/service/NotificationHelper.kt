package com.example.general_first_aid_kit.presentation.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.domain.model.NotificationType
import java.util.concurrent.atomic.AtomicInteger

object NotificationHelper {

    const val CHANNEL_EXPIRY = "channel_expiry"
    const val CHANNEL_STOCK = "channel_stock"
    const val CHANNEL_ACTIVITY = "channel_activity"

    private val notificationId = AtomicInteger(1000)

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_EXPIRY, "Срок годности", NotificationManager.IMPORTANCE_DEFAULT)
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_STOCK, "Остаток", NotificationManager.IMPORTANCE_DEFAULT)
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ACTIVITY, "Активность участников", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    fun showNotification(context: Context, type: NotificationType, title: String, body: String) {
        val channelId = when (type) {
            NotificationType.EXPIRY_WARNING, NotificationType.EXPIRED -> CHANNEL_EXPIRY
            NotificationType.LOW_STOCK -> CHANNEL_STOCK
            else -> CHANNEL_ACTIVITY
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.baseline_medication_24)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId.getAndIncrement(), notification)
    }
}

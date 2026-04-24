package com.example.general_first_aid_kit.presentation.service

import com.example.general_first_aid_kit.domain.model.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var firestore: FirebaseFirestore

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: return
        val body = message.notification?.body ?: return
        val typeRaw = message.data["type"] ?: return

        val type = runCatching { NotificationType.valueOf(typeRaw) }.getOrNull() ?: return

        NotificationHelper.showNotification(this, type, title, body)
    }

    override fun onNewToken(token: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
    }
}

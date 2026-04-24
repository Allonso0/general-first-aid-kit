package com.example.general_first_aid_kit.data.repository

import com.example.general_first_aid_kit.domain.model.AppNotification
import com.example.general_first_aid_kit.domain.model.NotificationType
import com.example.general_first_aid_kit.domain.repository.NotificationRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    override fun observeNotifications(userId: String): Flow<List<AppNotification>> = callbackFlow {
        val subscription = firestore
            .collection("users").document(userId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    runCatching {
                        AppNotification(
                            id = doc.id,
                            kitId = doc.getString("kitId") ?: "",
                            kitName = doc.getString("kitName") ?: "",
                            type = NotificationType.valueOf(doc.getString("type") ?: "EXPIRED"),
                            message = doc.getString("message") ?: "",
                            timestamp = when (val raw = doc.get("timestamp")) {
                                is Long -> raw
                                is Double -> raw.toLong()
                                is Timestamp -> raw.toDate().time
                                else -> 0L
                            },
                            isRead = doc.getBoolean("isRead") ?: false
                        )
                    }.getOrNull()
                } ?: emptyList()
                trySend(notifications)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun saveNotification(userId: String, notification: AppNotification) {
        withContext(Dispatchers.IO) {
            val col = firestore.collection("users").document(userId).collection("notifications")
            val docRef = if (notification.id.isEmpty()) col.document() else col.document(notification.id)
            docRef.set(
                mapOf(
                    "kitId" to notification.kitId,
                    "kitName" to notification.kitName,
                    "type" to notification.type.name,
                    "message" to notification.message,
                    "timestamp" to notification.timestamp,
                    "isRead" to notification.isRead
                )
            ).await()
        }
    }

    override suspend fun markAllAsRead(userId: String) {
        withContext(Dispatchers.IO) {
            val col = firestore.collection("users").document(userId).collection("notifications")
            val unread = col.whereEqualTo("isRead", false).get().await()
            if (unread.isEmpty) return@withContext
            val batch = firestore.batch()
            unread.documents.forEach { batch.update(it.reference, "isRead", true) }
            batch.commit().await()
        }
    }

    override suspend fun deleteAllNotifications(userId: String) {
        withContext(Dispatchers.IO) {
            val col = firestore.collection("users").document(userId).collection("notifications")
            val all = col.get().await()
            if (all.isEmpty) return@withContext
            val batch = firestore.batch()
            all.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
    }
}

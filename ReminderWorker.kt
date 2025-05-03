package com.example.subscriptionsapp.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.subscriptionsapp.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = FirebaseFirestore.getInstance()
            val notificationsCollection = db.collection("notifications")

            // Сегодняшняя дата
            val calendar = Calendar.getInstance()
            val today = calendar.time

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayString = formatter.format(today)

            val snapshot = notificationsCollection.get().await()

            for (document in snapshot.documents) {
                val nextPaymentDate = document.getDate("date")
                val name = document.getString("name") ?: "Подписка"
                val cost = document.getDouble("cost") ?: 0.0

                if (nextPaymentDate != null) {
                    val nextPaymentDateString = formatter.format(nextPaymentDate)

                    if (nextPaymentDateString == todayString) {
                        // Если дата платежа сегодня, отправляем уведомление
                        sendNotification(name, cost)
                    }
                }
        }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun sendNotification(name: String, cost: Double) {
        val channelId = "payment_reminder_channel"
        createNotificationChannel(channelId)

        val notificationText = "Завтра спишется ${"%.2f".format(cost)} ₽ за подписку \"$name\""

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Напоминание о списании")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(name.hashCode(), builder.build())
        }
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Напоминания о подписках"
            val descriptionText = "Канал для уведомлений о списании подписок"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

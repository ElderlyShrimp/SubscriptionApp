package com.example.subscriptionsapp

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.example.subscriptionsapp.models.Subscription
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text

fun getNextPaymentDate(lastPaymentDate: Date, recurrenceAmount: Int, recurrenceUnit: String): Date {
    val calendar = Calendar.getInstance()
    calendar.time = lastPaymentDate

    // Сбрасываем время
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val currentDate = Calendar.getInstance()
    currentDate.set(Calendar.HOUR_OF_DAY, 0)
    currentDate.set(Calendar.MINUTE, 0)
    currentDate.set(Calendar.SECOND, 0)
    currentDate.set(Calendar.MILLISECOND, 0)

    // Пока дата следующего списания в прошлом — прибавляем период
    while (calendar.before(currentDate)) {
        when (recurrenceUnit.lowercase()) {
            "дней" -> calendar.add(Calendar.DATE, recurrenceAmount)
            "недель" -> calendar.add(Calendar.WEEK_OF_YEAR, recurrenceAmount)
            "месяцев" -> calendar.add(Calendar.MONTH, recurrenceAmount)
            "лет" -> calendar.add(Calendar.YEAR, recurrenceAmount)
            else -> calendar.add(Calendar.DATE, recurrenceAmount) // по умолчанию как дни
        }
    }

    return calendar.time
}
@Composable
fun SubscriptionDetailScreen(subscriptionId: String, navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val subscription = remember { mutableStateOf<Subscription?>(null) }
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    // Логика загрузки данных из Firestore
    LaunchedEffect(subscriptionId) {
        db.collection("subscriptions").document(subscriptionId).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val data = document.data
                subscription.value = if (data != null) {
                    Subscription(
                        id = document.id,
                        subscriptionName = data["subscriptionName"] as? String ?: "",
                        category = data["category"] as? String ?: "",
                        cost = (data["cost"] as? Number)?.toDouble() ?: 0.0,
                        paymentType = data["paymentType"] as? String ?: "",
                        comment = data["comment"] as? String ?: "",
                        isNotificationChecked = data["isNotificationChecked"] as? Boolean ?: false,
                        isActive = data["isActive"] as? Boolean ?: true,
                        nextPaymentDate = (data["nextPaymentDate"] as? com.google.firebase.Timestamp)?.toDate() ?: (data["nextPaymentDate"] as? Date ?: Date()),
                        startDate = data["startDate"] as? Date ?: Date(),
                        recurrenceAmount = (data["recurrenceAmount"] as? Number)?.toInt() ?: 0,
                        recurrenceUnit = data["recurrenceUnit"] as? String ?: "дней"
                    )
                } else {
                    null
                }
            }
        }
    }

    // Когда данные загрузятся, отображаем их
    subscription.value?.let { sub ->
        val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(sub.nextPaymentDate)

        Column(modifier = Modifier.padding(16.dp)) {
            Text("Название подписки: ${sub.subscriptionName}", style = MaterialTheme.typography.titleMedium)
            Text("Стоимость: ${sub.cost} ₽")
            Text("Категория: ${sub.category}")
            Text("Периодичность: ${sub.paymentType}")
            Text("Следующее списание: $formattedDate")

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Button(onClick = { /* handle edit */ }) {
                    Text("Изменить")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { showDialog = true }) {
                    Text("Удалить")
                }
            }
        }
    }

    // Окно подтверждения удаления
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Удаление подписки") },
            text = { Text("Вы уверены, что хотите удалить эту подписку?") },
            confirmButton = {
                TextButton(onClick = {
                    // Удаляем подписку
                    db.collection("subscriptions").document(subscriptionId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Подписка удалена", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Ошибка удаления: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    showDialog = false
                }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

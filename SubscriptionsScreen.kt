package com.example.subscriptionsapp

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.example.subscriptionsapp.models.Subscription
import androidx.compose.material3.Button
import androidx.navigation.compose.rememberNavController
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.clickable
import android.util.Log

@Composable
fun SubscriptionsScreen(navController: NavHostController) {  // Поменяли тип с NavController на NavHostController
    val db = FirebaseFirestore.getInstance()
    var subscriptions by remember { mutableStateOf<List<Subscription>>(emptyList()) }

    // Загрузка данных из Firestore
    LaunchedEffect(Unit) {
        db.collection("subscriptions").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val loaded = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    if (data != null) {
                        Subscription(
                            id = doc.id,
                            subscriptionName = data["subscriptionName"] as? String ?: "",
                            category = data["category"] as? String ?: "",
                            cost = (data["cost"] as? Number)?.toDouble() ?: 0.0,
                            paymentType = data["paymentType"] as? String ?: "",
                            comment = data["comment"] as? String ?: "",
                            isNotificationChecked = data["isNotificationChecked"] as? Boolean ?: false,
                            isActive = data["isActive"] as? Boolean ?: true,
                            nextPaymentDate = (data["nextPaymentDate"] as? com.google.firebase.Timestamp)?.toDate() ?: (data["nextPaymentDate"] as? Date ?: Date()),
                            recurrenceAmount = (data["recurrenceAmount"] as? Number)?.toInt() ?: 0,
                            recurrenceUnit = data["recurrenceUnit"] as? String ?: "дней"
                        )
                    } else null
                }
                subscriptions = loaded
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Все подписки", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(subscriptions) { subscription ->
                SubscriptionCard(subscription, navController)  // Передаем navController в SubscriptionCard
            }
        }
    }
}

@Composable
fun SubscriptionCard(subscription: Subscription, navController: NavHostController) {
    // Форматируем уже сохраненную дату nextPaymentDate
    val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(subscription.nextPaymentDate)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable {
                // Навигация на экран с деталями подписки
                navController.navigate("subscriptionDetail/${subscription.id}")
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = subscription.subscriptionName,
                style = MaterialTheme.typography.titleMedium
            )
            Text("Стоимость: ${subscription.cost} ₽")
            Text("Периодичность: ${subscription.paymentType}")
            Text("Следующее списание: $formattedDate") // Отображаем правильно отформатированную дату
            if (!subscription.isActive) {
                Text("Статус: Приостановлена")
            }
        }
    }
}


fun formatNextPaymentDate(subscription: Subscription): String {
    val nextDate = getNextPaymentDate(
        subscription.nextPaymentDate, // Исправлено: используем nextPaymentDate вместо startDate
        subscription.recurrenceAmount,
        subscription.recurrenceUnit
    )
    return SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(nextDate)
}

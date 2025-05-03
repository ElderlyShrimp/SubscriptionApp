package com.example.subscriptionsapp

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.subscriptionsapp.models.Subscription
import com.example.subscriptionsapp.utils.calculateNextPaymentDate
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.work.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.workDataOf
import com.example.subscriptionsapp.work.ReminderWorker
import java.util.concurrent.TimeUnit

// Импорт для NotificationManagerCompat:
import androidx.core.app.NotificationManagerCompat

@Composable
fun AddSubscriptionScreen(navController: NavHostController) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var name by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("₽") }
    var recurrenceAmount by remember { mutableStateOf("") }
    var recurrenceUnit by remember { mutableStateOf("дней") }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var notify by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> imageUri = uri }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            startDate = calendar.time
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val isFormValid = name.isNotBlank() && cost.isNotBlank() && recurrenceAmount.isNotBlank() && startDate != null

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Image picker
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).background(Color.LightGray, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(100.dp).background(Color.LightGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", fontSize = 40.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField("Название подписки", name, { if (it.length <= 50) name = it })
        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField("Стоимость", cost, { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) cost = it }, KeyboardType.Number)
        Spacer(modifier = Modifier.height(8.dp))

        DropdownMenuBox(current = currency, options = listOf("₽", "$", "€", "£"), onSelect = { currency = it })
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            CustomTextField("Каждые", recurrenceAmount, { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) recurrenceAmount = it }, KeyboardType.Number, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            DropdownMenuBox(current = recurrenceUnit, options = listOf("дней", "недель", "месяцев"), onSelect = { recurrenceUnit = it }, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { datePickerDialog.show() }) {
            Text(startDate?.toString() ?: "Выбрать дату начала")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = notify, onCheckedChange = { notify = it })
            Text("Уведомление о списаниях")
        }

        Spacer(modifier = Modifier.height(8.dp))
        CustomTextField("Категория", category, { category = it })
        Spacer(modifier = Modifier.height(8.dp))
        CustomTextField("Платёжный метод", paymentMethod, { paymentMethod = it })

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val costValue = cost.toDoubleOrNull() ?: return@Button
                val recurrence = recurrenceAmount.toIntOrNull() ?: return@Button
                val sub = Subscription(
                    subscriptionName = name,
                    category = category,
                    cost = costValue,
                    paymentType = "$recurrence $recurrenceUnit",
                    comment = "",
                    startDate = startDate ?: Date(),
                    isNotificationChecked = notify,  // Сохраняем состояние флажка в базе данных
                    nextPaymentDate = getNextPaymentDate(
                        startDate ?: Date(),
                        recurrence,
                        recurrenceUnit
                    ),
                    isActive = true,
                    recurrenceAmount = recurrence,
                    recurrenceUnit = recurrenceUnit
                )

                // Добавляем подписку в Firestore
                FirebaseFirestore.getInstance().collection("subscriptions")
                    .add(sub)
                    .addOnSuccessListener { documentReference ->
                        // Если уведомления включены, добавляем запись в коллекцию notifications
                        if (notify) {
                            val notificationData = hashMapOf(
                                "name" to sub.subscriptionName,
                                "cost" to sub.cost,
                                "nextPaymentDate" to sub.nextPaymentDate
                            )

                            // Добавляем запись в коллекцию notifications
                            FirebaseFirestore.getInstance().collection("notifications")
                                .document(documentReference.id) // Ссылка на подписку
                                .set(notificationData)
                                .addOnSuccessListener {
                                    Log.d("AddSubscription", "Уведомление добавлено успешно")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("AddSubscription", "Ошибка при добавлении уведомления: ${e.localizedMessage}")
                                }
                        }

                        // Навигация после добавления
                        navController.navigate("subscriptions") {
                            popUpTo("add") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("AddSubscription", "Ошибка при добавлении: ${e.localizedMessage}")
                    }
            },
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormValid) MaterialTheme.colorScheme.primary else Color.LightGray
            )
        ) {
            Text("Добавить подписку")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(label: String, value: String, onValueChange: (String) -> Unit, keyboardType: KeyboardType = KeyboardType.Text, modifier: Modifier = Modifier.fillMaxWidth()) {
    Log.d("Debug", "Rendering CustomTextField: $label")
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        colors = TextFieldDefaults.textFieldColors(
            containerColor = if (value.isEmpty()) Color(0xFFF0F0F0) else Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
fun DropdownMenuBox(current: String, options: List<String>, onSelect: (String) -> Unit, modifier: Modifier = Modifier.fillMaxWidth()) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.clickable { expanded = true }.padding(8.dp)) {
        Text(current)
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onSelect(option)
                    expanded = false
                })
            }
        }
    }
}

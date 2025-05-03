package com.example.subscriptionsapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen(navController: NavHostController) {
    var subscriptions by remember { mutableStateOf<List<String>>(emptyList()) }
    val firestore = FirebaseFirestore.getInstance()

    // Получение данных из Firestore
    LaunchedEffect(Unit) {
        firestore.collection("subscriptions")
            .get()
            .addOnSuccessListener { result ->
                val list = result.mapNotNull { it.getString("name") }
                subscriptions = list
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Мои подписки", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(subscriptions) { subscription ->
                Text(text = subscription, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate("add_subscription")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить подписку")
        }
    }
}


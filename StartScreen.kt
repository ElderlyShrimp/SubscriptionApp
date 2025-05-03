package com.example.subscriptionsapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun StartScreen(navController: NavHostController) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser != null) {
        // Пользователь уже авторизован — переходим на главный экран
        navController.navigate("home") {
            popUpTo("start") { inclusive = true }
        }
    } else {
        // Пользователь не авторизован — показываем логин
        navController.navigate("login") {
            popUpTo("start") { inclusive = true }
        }
    }
}

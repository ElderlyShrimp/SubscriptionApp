package com.example.subscriptionsapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "Главная", Icons.Default.Home)
    object Subscriptions : BottomNavItem("subscriptions", "Подписки", Icons.Default.List)
    object Add : BottomNavItem("add_subscription", "Новая", Icons.Default.Add)
    object History : BottomNavItem("history", "История", Icons.Default.Notifications)
    object Profile : BottomNavItem("profile", "Профиль", Icons.Default.Person)
}

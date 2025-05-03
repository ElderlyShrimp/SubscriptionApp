package com.example.subscriptionsapp.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Subscriptions,
        BottomNavItem.Add,
        BottomNavItem.History,
        BottomNavItem.Profile
    )

    NavigationBar {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        // Переход на другую вкладку
                        navController.navigate(item.route) {
                            // Очистка всех предыдущих экранов в стеке навигации
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true  // Убираем все предыдущие экраны
                            }
                            launchSingleTop = true  // Только один экран на старте
                            restoreState = true
                        }
                    } else {
                        // Если текущая вкладка уже активна, ничего не делаем или очищаем её
                        navController.popBackStack(item.route, inclusive = false)
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = false // Это поможет принудительно перезапустить экран
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) }
            )
        }
    }
}

package com.example.subscriptionsapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.subscriptionsapp.*
import com.example.subscriptionsapp.AddSubscriptionScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)  // Передаем navController в BottomNavBar
        }
    ) { innerPadding ->
        // Используем переданный navController
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(navController)  // Передаем navController в HomeScreen
            }
            composable(BottomNavItem.Subscriptions.route) {
                SubscriptionsScreen(navController)  // Передаем navController в SubscriptionsScreen
            }
            composable(BottomNavItem.Add.route) {
                AddSubscriptionScreen(navController)  // Передаем navController в AddSubscriptionScreen
            }
            composable(BottomNavItem.History.route) {
                HistoryScreen()
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen()
            }
            composable("subscriptionDetail/{subscriptionId}") { backStackEntry ->
                val subscriptionId = backStackEntry.arguments?.getString("subscriptionId") ?: ""
                SubscriptionDetailScreen(subscriptionId, navController)  // Передаем navController в SubscriptionDetailScreen
            }
        }
    }
}

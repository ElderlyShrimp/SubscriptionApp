package com.example.subscriptionsapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.example.subscriptionsapp.navigation.AppNavigation
import com.example.subscriptionsapp.ui.theme.SubscriptionsAppTheme
import com.example.subscriptionsapp.work.ReminderWorker
import com.google.firebase.FirebaseApp
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Разрешение на уведомления получено!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Разрешение на уведомления отклонено.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Настройка WorkManager для напоминаний
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(24, TimeUnit.HOURS)  // Отложить на 24 часа
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)

        setContent {
            SubscriptionsAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavigation(navController)
                }
            }
        }
    }
}

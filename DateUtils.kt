package com.example.subscriptionsapp.utils

import java.text.SimpleDateFormat
import java.util.*

fun calculateNextPaymentDate(startDate: Date, paymentType: String): Date {
    val calendar = Calendar.getInstance()
    calendar.time = startDate

    when (paymentType) {
        "monthly" -> calendar.add(Calendar.MONTH, 1)
        "yearly" -> calendar.add(Calendar.YEAR, 1)
        "weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
    }

    return calendar.time
}


package com.example.subscriptionsapp.models

import java.util.*

data class Subscription(
    val id: String = "", // ID из Firestore
    val subscriptionName: String,  // Название подписки
    val category: String,          // Категория подписки
    val cost: Double,              // Стоимость подписки
    val paymentType: String,       // Тип оплаты
    val comment: String?,          // Комментарий
    val isNotificationChecked: Boolean, // Флаг уведомлений
    val isActive: Boolean,         // Активна ли подписка
    val nextPaymentDate: Date,     // Дата следующего списания
    val startDate: Date = Date(),   // Дата начала подписки (по умолчанию текущая дата)
    val recurrenceAmount: Int = 0,      // Количество единиц для расчета (например, 1 день, 2 недели)
    val recurrenceUnit: String = "дней"  // Единица измерения для регулярности (например, день, неделя, месяц)
)

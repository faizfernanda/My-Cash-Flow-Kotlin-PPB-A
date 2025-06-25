package com.example.mymoneynotes.model

data class Transaction(
    val id: Int = 0,
    val type: TransactionType,
    val category: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis()
)

enum class TransactionType {
    INCOME,
    EXPENSE
}

package com.example.mymoneynotes.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.example.mymoneynotes.model.Transaction
import com.example.mymoneynotes.model.TransactionType
import com.example.mymoneynotes.repository.TransactionRepository

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private var nextId = 0
    private val repo = TransactionRepository(application.applicationContext)
    val transactions = mutableStateListOf<Transaction>()

    init {
        transactions.addAll(repo.load())
        nextId = (transactions.maxOfOrNull { it.id } ?: -1) + 1
    }

    private fun persist() {
        repo.save(transactions)
    }

    fun addTransaction(type: TransactionType, category: String, amount: Double, date: Long) {
        transactions.add(Transaction(nextId++, type, category, amount, date))
        persist()
    }

    fun updateTransaction(updated: Transaction) {
        val index = transactions.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            transactions[index] = updated
            persist()
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        transactions.removeAll { it.id == transaction.id }
        persist()
    }
}
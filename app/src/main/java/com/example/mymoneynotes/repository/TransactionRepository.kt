package com.example.mymoneynotes.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.mymoneynotes.model.Transaction
import com.example.mymoneynotes.model.TransactionType
import org.json.JSONArray
import org.json.JSONObject

class TransactionRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("transactions", Context.MODE_PRIVATE)

    fun load(): MutableList<Transaction> {
        val json = prefs.getString(KEY_TRANSACTIONS, "[]") ?: "[]"
        val array = JSONArray(json)
        val list = mutableListOf<Transaction>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                Transaction(
                    id = obj.getInt("id"),
                    type = TransactionType.valueOf(obj.getString("type")),
                    category = obj.getString("category"),
                    amount = obj.getDouble("amount"),
                    date = obj.getLong("date")
                )
            )
        }
        return list
    }

    fun save(list: List<Transaction>) {
        val array = JSONArray()
        list.forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("type", t.type.name)
            obj.put("category", t.category)
            obj.put("amount", t.amount)
            obj.put("date", t.date)
            array.put(obj)
        }
        prefs.edit().putString(KEY_TRANSACTIONS, array.toString()).apply()
    }

    companion object {
        private const val KEY_TRANSACTIONS = "transactions_json"
    }
}

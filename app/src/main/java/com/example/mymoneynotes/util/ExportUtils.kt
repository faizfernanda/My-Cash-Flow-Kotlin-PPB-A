package com.example.mymoneynotes.util

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.mymoneynotes.model.Transaction
import com.example.mymoneynotes.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ExportUtils {
    fun export(context: Context, transactions: List<Transaction>): File? {
        return try {
            val doc = PdfDocument()
            val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
            val canvas = page.canvas
            val paint = android.graphics.Paint().apply { textSize = 12f }
            var y = 40

            val income = transactions.filter { it.type == TransactionType.INCOME }
            val expense = transactions.filter { it.type == TransactionType.EXPENSE }

            fun drawSection(title: String, list: List<Transaction>) {
                canvas.drawText(title, 20f, y.toFloat(), paint)
                y += 20
                canvas.drawText("Tanggal  Kategori  Nominal", 20f, y.toFloat(), paint)
                y += 20
                list.forEach {
                    val date = DateTimeFormatter.ISO_DATE.format(
                        java.time.Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
                    )
                    canvas.drawText("$date  ${it.category}  ${"%,.0f".format(it.amount)}", 20f, y.toFloat(), paint)
                    y += 20
                }
                y += 20
            }

            drawSection("Pemasukan", income)
            drawSection("Pengeluaran", expense)

            val totalIncome = income.sumOf { it.amount }
            val totalExpense = expense.sumOf { it.amount }
            val balance = totalIncome - totalExpense

            canvas.drawText("Total Pemasukan : ${"%,.0f".format(totalIncome)}", 20f, y.toFloat(), paint)
            y += 20
            canvas.drawText("Total Pengeluaran : ${"%,.0f".format(totalExpense)}", 20f, y.toFloat(), paint)
            y += 20
            canvas.drawText("Total Balance : ${"%,.0f".format(balance)}", 20f, y.toFloat(), paint)

            doc.finishPage(page)
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (dir != null && !dir.exists()) dir.mkdirs()
            val file = File(dir, "transactions.pdf")
            FileOutputStream(file).use { out -> doc.writeTo(out) }
            doc.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

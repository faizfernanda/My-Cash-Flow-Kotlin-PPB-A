package com.example.mymoneynotes

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mymoneynotes.model.Transaction
import com.example.mymoneynotes.model.TransactionType
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.Instant
import androidx.compose.ui.text.style.TextOverflow

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    transactions: List<Transaction>,
    onAddClicked: () -> Unit,
    onItemClick: (Transaction) -> Unit,
    onReport: () -> Unit
) {
    val categories = listOf("Semua") + transactions.map { it.category }.distinct()
    var selectedCategory by remember { mutableStateOf("Semua") }
    var filterType by remember { mutableStateOf<Int>(0) } //0=all,1=income,2=expense
    val shown = remember(transactions, selectedCategory, filterType) {
        transactions.filter { t ->
            (filterType == 0 || (filterType == 1 && t.type == TransactionType.INCOME) || (filterType == 2 && t.type == TransactionType.EXPENSE)) &&
                    (selectedCategory == "Semua" || t.category == selectedCategory)
        }
    }

    var sort by remember { mutableStateOf(1) } //1=newest,0=oldest,2=amount asc,3=amount desc
    var showAll by remember { mutableStateOf(false) }

    val filtered = remember(shown, sort) {
        val base = when (sort) {
            0 -> shown.sortedBy { it.date }
            1 -> shown.sortedByDescending { it.date }
            2 -> shown.sortedBy { it.amount }
            3 -> shown.sortedByDescending { it.amount }
            else -> shown
        }
        base
    }

    val displayList = if (showAll || filtered.size <= 50) filtered else filtered.take(50)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cash Flow") },
                actions = {
                    IconButton(onClick = onReport) {
                        Icon(Icons.Default.Assessment, contentDescription = "Laporan")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClicked) {
                Text("+")
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
        ) {
            TotalBalance(transactions)
            TransactionChart(transactions)

            Row(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kategori
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Kategori:")
                    Spacer(Modifier.width(8.dp))
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Button(onClick = { expanded = true }) {
                            Text(
                                text = selectedCategory,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 80.dp)
                            )
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            categories.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat) }, onClick = {
                                    selectedCategory = cat
                                    expanded = false
                                })
                            }
                        }
                    }
                }

                // Sort
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sort:")
                    Spacer(Modifier.width(8.dp))
                    var exp by remember { mutableStateOf(false) }
                    val label = when (sort) {
                        0 -> "Tanggal Terlama"
                        1 -> "Tanggal Terbaru"
                        2 -> "Nominal Kecil"
                        3 -> "Nominal Besar"
                        else -> "Tanggal Terbaru"
                    }
                    Box {
                        Button(onClick = { exp = true }) { Text(label) }
                        DropdownMenu(expanded = exp, onDismissRequest = { exp = false }) {
                            DropdownMenuItem(text = { Text("Tanggal Terlama") }, onClick = { sort = 0; exp = false })
                            DropdownMenuItem(text = { Text("Tanggal Terbaru") }, onClick = { sort = 1; exp = false })
                            DropdownMenuItem(text = { Text("Nominal Kecil") }, onClick = { sort = 2; exp = false })
                            DropdownMenuItem(text = { Text("Nominal Besar") }, onClick = { sort = 3; exp = false })
                        }
                    }
                }
            }

            Row(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = filterType == 0, onClick = { filterType = 0 }, label = { Text("Semua") })
                FilterChip(selected = filterType == 1, onClick = { filterType = 1 }, label = { Text("Pemasukan") })
                FilterChip(selected = filterType == 2, onClick = { filterType = 2 }, label = { Text("Pengeluaran") })
            }

            LazyColumn {
                items(displayList) { transaction ->
                    val isIncome = transaction.type == TransactionType.INCOME
                    val labelColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336) // Hijau / Merah

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { onItemClick(transaction) },
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)) {

                            // Label di pojok kanan atas
                            Text(
                                text = if (isIncome) "Pemasukan" else "Pengeluaran",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .background(labelColor, shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )

                            Column(
                                modifier = Modifier.align(Alignment.TopStart)
                            ) {
                                // Kategori di kiri atas
                                Text(
                                    text = transaction.category,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 300.dp)
                                )
                                Text(
                                    DateTimeFormatter.ISO_DATE.format(
                                        java.time.Instant.ofEpochMilli(transaction.date).atZone(ZoneId.systemDefault()).toLocalDate()
                                    ),
                                    style = MaterialTheme.typography.labelSmall
                                )

                                // Jumlah di bawah kategori
                                Text(text = "Rp ${"%,.0f".format(transaction.amount)}")
                            }
                        }
                    }
                }
                if (!showAll && filtered.size > 50) {
                    item {
                        TextButton(
                            onClick = { showAll = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Tampilkan Semua") }
                    }
                }
            }
        }
    }

}

@Composable
fun TransactionChart(transactions: List<Transaction>) {
    val incomeTotal = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expenseTotal = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val total = incomeTotal + expenseTotal

    val incomePercent = if (total > 0) incomeTotal / total else 0.0
    val expensePercent = if (total > 0) expenseTotal / total else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Grafik Pemasukan vs Pengeluaran", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)) {

            val incomeWidth = size.width * incomePercent.toFloat()
            val expenseWidth = size.width * expensePercent.toFloat()

            drawRect(
                color = Color(0xFF4CAF50), // Hijau untuk pemasukan
                size = Size(incomeWidth, size.height)
            )
            drawRect(
                color = Color(0xFFF44336), // Merah untuk pengeluaran
                topLeft = Offset(incomeWidth, 0f),
                size = Size(expenseWidth, size.height)
            )
        }

    }
}

@Composable
fun TotalBalance(transactions: List<Transaction>) {
    val incomeTotal = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expenseTotal = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val balance = incomeTotal - expenseTotal

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Total Balance", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Rp ${"%,.0f".format(balance)}",
            style = MaterialTheme.typography.headlineMedium,
            color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}



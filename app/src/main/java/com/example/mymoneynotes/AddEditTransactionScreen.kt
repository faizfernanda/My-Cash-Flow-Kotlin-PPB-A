package com.example.mymoneynotes
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mymoneynotes.model.Transaction
import com.example.mymoneynotes.model.TransactionType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    transaction: Transaction? = null,
    onSave: (TransactionType, String, Double, Long, Int?) -> Unit,
    onDelete: ((Transaction) -> Unit)? = null,
    onCancel: () -> Unit
) {
    var type by remember { mutableStateOf(transaction?.type ?: TransactionType.INCOME) }
    var category by remember { mutableStateOf(transaction?.category ?: "") }
    var amount by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var dateMillis by remember { mutableStateOf(transaction?.date ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatted = remember(dateMillis) {
        DateTimeFormatter.ISO_DATE.format(
            Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        )
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (transaction == null) "Tambah Transaksi" else "Edit Transaksi") })
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row {
                RadioButton(
                    selected = type == TransactionType.INCOME,
                    onClick = { type = TransactionType.INCOME }
                )
                Text("Pemasukan")

                Spacer(modifier = Modifier.width(16.dp))

                RadioButton(
                    selected = type == TransactionType.EXPENSE,
                    onClick = { type = TransactionType.EXPENSE }
                )
                Text("Pengeluaran")
            }

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Kategori") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Nominal") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            dateMillis = datePickerState.selectedDateMillis ?: dateMillis
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
                    }
                ) { DatePicker(state = datePickerState) }
            }

            OutlinedTextField(
                value = dateFormatted,
                onValueChange = {},
                label = { Text("Tanggal") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                    }
                }
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onCancel) {
                    Text("Batal")
                }
                if (transaction != null && onDelete != null) {
                    Button(onClick = { onDelete(transaction) }) {
                        Text("Hapus")
                    }
                }
                Button(onClick = {
                    dateMillis = datePickerState.selectedDateMillis ?: dateMillis
                    onSave(
                        type,
                        category,
                        amount.toDoubleOrNull() ?: 0.0,
                        dateMillis,
                        transaction?.id
                    )
                }) {
                    Text("Simpan")
                }
            }
        }
    }
}

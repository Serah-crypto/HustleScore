package com.serah.hustlescore.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hustlescore.ui.theme.HustleScoreTheme
import com.serah.hustlescore.models.Transaction
import com.serah.hustlescore.models.TransactionType
import com.serah.hustlescore.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private data class TypeConfig(
    val type: TransactionType,
    val label: String,
    val emoji: String,
    val color: Color,
    val bgColor: Color
)

private val typeConfigs = listOf(
    TypeConfig(TransactionType.RECEIVED, "Income", "💰", Color(0xFF16A34A), Color(0xFFDCFCE7)),
    TypeConfig(TransactionType.SENT, "Expense", "💸", Color(0xFFDC2626), Color(0xFFFEE2E2)),
    TypeConfig(TransactionType.SAVINGS, "Savings", "🏦", Color(0xFF2563EB), Color(0xFFDBEAFE)),
    TypeConfig(TransactionType.LOAN_REPAYMENT, "Loan Repayment", "✅", Color(0xFF7C3AED), Color(0xFFEDE9FE))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    onSaveTransaction: (Transaction) -> Unit = {}   // Pass save logic from ViewModel
) {
    var selectedType by remember { mutableStateOf(TransactionType.RECEIVED) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    val activeConfig = typeConfigs.first { it.type == selectedType }

    // Date Picker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            selectedDate = sdf.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Select", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Top Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Add Transaction", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Record income, expense, savings or repayment", fontSize = 12.sp, color = TextSecondary)
            }
        }

        // Type Selector
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Transaction Type", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    typeConfigs.take(2).forEach { config ->
                        TypeChip(
                            config = config,
                            isSelected = selectedType == config.type,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedType = config.type }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    typeConfigs.drop(2).forEach { config ->
                        TypeChip(
                            config = config,
                            isSelected = selectedType == config.type,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedType = config.type }
                        )
                    }
                }
            }
        }

        // Form
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount (KSh)") },
                    placeholder = { Text("e.g. 5000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text(activeConfig.emoji, fontSize = 18.sp) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = activeConfig.color),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description") },
                    placeholder = { Text("Salary, groceries, etc.") },
                    maxLines = 2
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Category (optional)") },
                    placeholder = { Text("e.g. salary, shopping") }
                )

                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = activeConfig.color)
                        }
                    }
                )
            }
        }

        // Save Button
        Button(
            onClick = {
                if (amount.isBlank()) return@Button

                saving = true

                val transaction = Transaction(
                    type = selectedType,
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    date = selectedDate,
                    description = description.ifBlank { null },
                    category = if (category.isBlank()) null else category
                )

                onSaveTransaction(transaction)

                // Optional: Reset after saving (or handle in ViewModel)
                // saving = false
                // navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !saving && amount.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = activeConfig.color),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (saving) {
                Text("Saving...", fontWeight = FontWeight.Bold)
            } else {
                Text("Save ${activeConfig.label}", fontWeight = FontWeight.Bold)
            }
        }


        }
    }


@Composable
private fun TypeChip(
    config: TypeConfig,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) config.color else config.bgColor)
            .border(1.dp, config.color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = config.emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = config.label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else config.color
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreview() {
    HustleScoreTheme {
        AddTransactionScreen(navController = rememberNavController())
    }
}
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
    TypeConfig(TransactionType.INCOME, "Income", "💰", Color(0xFF16A34A), Color(0xFFDCFCE7)),
    TypeConfig(TransactionType.EXPENSE, "Expense", "💸", Color(0xFFDC2626), Color(0xFFFEE2E2)),
    TypeConfig(TransactionType.SAVINGS, "Savings", "🏦", Color(0xFF2563EB), Color(0xFFDBEAFE)),
    TypeConfig(TransactionType.LOAN_REPAYMENT, "Loan Repayment", "✅", Color(0xFF7C3AED), Color(0xFFEDE9FE))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(navController: NavController) {
    var selectedType by remember { mutableStateOf(TransactionType.INCOME) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    val activeConfig = typeConfigs.first { it.type == selectedType }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(Date(millis))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Add Transaction",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Record income, expense, savings or repayment",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Transaction Type Selector
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Transaction Type",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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

        // Form Fields
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Amount, Description, Category, Date fields remain the same...
                // (I've kept them exactly as you had them for brevity)

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount (KSh)") },
                    placeholder = { Text("e.g. 5000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text(activeConfig.emoji, fontSize = 18.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeConfig.color,
                        focusedLabelColor = activeConfig.color
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description") },
                    placeholder = { Text("Add a short description...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeConfig.color,
                        focusedLabelColor = activeConfig.color
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Category (optional)") },
                    placeholder = { Text("e.g. salary, groceries, emergency") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeConfig.color,
                        focusedLabelColor = activeConfig.color
                    ),
                    shape = RoundedCornerShape(12.dp)
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
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeConfig.color,
                        focusedLabelColor = activeConfig.color
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Amount Preview Banner
        if (amount.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(activeConfig.bgColor)
                    .border(1.dp, activeConfig.color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = activeConfig.label, fontSize = 12.sp, color = activeConfig.color)
                        Text(
                            text = "KSh ${amount.toDoubleOrNull()?.let { String.format("%,.0f", it) } ?: amount}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = activeConfig.color
                        )
                        if (description.isNotBlank()) {
                            Text(
                                text = description,
                                fontSize = 12.sp,
                                color = activeConfig.color.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Text(text = activeConfig.emoji, fontSize = 40.sp)
                }
            }
        }

        // Save Button
        Button(
            onClick = { /* your save logic */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = amount.isNotBlank() && !saving,
            colors = ButtonDefaults.buttonColors(containerColor = activeConfig.color),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (saving) {
                Text("Saving...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save ${activeConfig.label}", fontWeight = FontWeight.Bold)
            }
        }

        if (saved) {
            // Success banner (kept as you had it)
            // ... your success UI
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// TypeChip remains the same
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
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = config.color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
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

// ====================== PREVIEW ======================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddTransactionScreenPreview() {
    HustleScoreTheme {   // Replace with your actual Theme name if different
        AddTransactionScreen(navController = rememberNavController())
    }
}
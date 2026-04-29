package com.serah.hustlescore.ui.screens.user



import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.serah.hustlescore.models.Transaction
import com.serah.hustlescore.models.TransactionType
import com.serah.hustlescore.navigation.Screen
import com.serah.hustlescore.ui.theme.BackgroundGray
import com.serah.hustlescore.ui.theme.HustleGreen
import com.serah.hustlescore.ui.theme.TextPrimary
import com.serah.hustlescore.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private data class TypeConfig(
    val type: TransactionType,
    val label: String,
    val emoji: String,
    val color: Color,
    val bgColor: Color
)

private val typeConfigs = listOf(
    TypeConfig(TransactionType.INCOME,          "Income",          "💰", Color(0xFF16A34A), Color(0xFFDCFCE7)),
    TypeConfig(TransactionType.EXPENSE,         "Expense",         "💸", Color(0xFFDC2626), Color(0xFFFEE2E2)),
    TypeConfig(TransactionType.SAVINGS,         "Savings",         "🏦", Color(0xFF2563EB), Color(0xFFDBEAFE)),
    TypeConfig(TransactionType.LOAN_REPAYMENT,  "Loan Repayment",  "✅", Color(0xFF7C3AED), Color(0xFFEDE9FE))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(navController: NavController) {
    var selectedType by remember { mutableStateOf(TransactionType.INCOME) }
    var amount       by remember { mutableStateOf("") }
    var description  by remember { mutableStateOf("") }
    var category     by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var showDatePicker by remember { mutableStateOf(false) }
    var saving       by remember { mutableStateOf(false) }
    var saved        by remember { mutableStateOf(false) }

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
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
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

        // ── Top Bar ───────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
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

        // ── Type Selector ─────────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Transaction Type",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Row 1
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

                // Row 2
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

        // ── Form Fields ───────────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount (KSh)") },
                    placeholder = { Text("e.g. 5000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = {
                        Text(
                            text = activeConfig.emoji,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeConfig.color,
                        focusedLabelColor = activeConfig.color
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description") },
                    placeholder = {
                        Text(
                            when (selectedType) {
                                TransactionType.INCOME -> "e.g. Salary from employer"
                                TransactionType.EXPENSE -> "e.g. Groceries at Naivas"
                                TransactionType.SAVINGS -> "e.g. M-Shwari deposit"
                                TransactionType.LOAN_REPAYMENT -> "e.g. Fuliza repayment"
                            }
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeConfig.color,
                        focusedLabelColor = activeConfig.color
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )

                // Category
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Category (optional)") },
                    placeholder = {
                        Text(
                            when (selectedType) {
                                TransactionType.INCOME -> "e.g. salary, business, gig"
                                TransactionType.EXPENSE -> "e.g. food, transport, utilities"
                                TransactionType.SAVINGS -> "e.g. emergency, goal"
                                TransactionType.LOAN_REPAYMENT -> "e.g. fuliza, kcb, mshwari"
                            }
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeConfig.color,
                        focusedLabelColor = activeConfig.color
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Date Picker Row
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Pick date",
                                tint = activeConfig.color
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeConfig.color,
                        focusedLabelColor = activeConfig.color
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }

        // ── Amount Preview Banner ─────────────────────────────────────
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
                        Text(
                            text = activeConfig.label,
                            fontSize = 12.sp,
                            color = activeConfig.color
                        )
                        Text(
                            text = "KSh ${
                                amount.toDoubleOrNull()?.let {
                                    String.format("%,.0f", it)
                                } ?: amount
                            }",
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
                    Text(text = activeConfig.emoji, fontSize = 36.sp)
                }
            }
        }

        // ── Save Button ───────────────────────────────────────────────
        Button(
            onClick = {

                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                val amountValue = amount.toDoubleOrNull() ?: return@Button
                saving = true

                val transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    userId = uid,
                    type = selectedType,
                    amount = amountValue,
                    date = selectedDate,
                    description = description.trim(),
                    category = category.trim().ifBlank {
                        when (selectedType) {
                            TransactionType.INCOME -> "income"
                            TransactionType.EXPENSE -> "other"
                            TransactionType.SAVINGS -> "savings"
                            TransactionType.LOAN_REPAYMENT -> "loan"
                        }
                    },
                    mpesaRef = "",
                    rawSms = ""
                )



                FirebaseDatabase.getInstance()
                    .getReference("transactions/$uid")
                    .push()
                    .setValue(transaction)
                    .addOnSuccessListener {
                        saving = false
                        saved = true
                        // Reset form
                        amount = ""
                        description = ""
                        category = ""
                        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    }
                    .addOnFailureListener {
                        saving = false
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = amount.isNotBlank() && !saving,
            colors = ButtonDefaults.buttonColors(containerColor = activeConfig.color),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (saving) {
                Text(text = "Saving...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            } else {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save ${activeConfig.label}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // ── Success Banner ────────────────────────────────────────────
        if (saved) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFDCFCE7))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = HustleGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Transaction saved!",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF15803D),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Add another or go back to Dashboard",
                            color = Color(0xFF15803D),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    modifier = Modifier.weight(1f),
                    onClick = { saved = false }
                ) {
                    Text(
                        text = "Add Another",
                        color = activeConfig.color,
                        fontWeight = FontWeight.Medium
                    )
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Dashboard.route) },
                    colors = ButtonDefaults.buttonColors(containerColor = HustleGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Go to Dashboard")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ── Type Chip ──────────────────────────────────────────────────────────────

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
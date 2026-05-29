package com.serah.hustlescore.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
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

// ─── Palette (mirrors HomeScreen) ────────────────────────────────────────────
private val GreenDeep    = Color(0xFF062110)
private val GreenMid     = Color(0xFF0F4523)
private val GreenBrand   = Color(0xFF1A7A3C)
private val GreenLight   = Color(0xFF25A355)
private val GreenAccent  = Color(0xFF4FCB78)
private val GreenPale    = Color(0xFFD6F0E0)
private val GreenSurface = Color(0xFFEEF8F2)
private val Cream        = Color(0xFFFAF8F4)
private val CreamCard    = Color(0xFFFFFDF9)
private val CreamBorder  = Color(0xFFE8E0D4)
private val Amber        = Color(0xFFD4860A)
private val AmberPale    = Color(0xFFFFF0D4)
private val Rust         = Color(0xFFC0521A)
private val RustPale     = Color(0xFFFFECE0)
private val Teal         = Color(0xFF1A7A6E)
private val TealPale     = Color(0xFFD4F0EE)
private val Plum         = Color(0xFF6E3A7A)
private val PlumPale     = Color(0xFFF0E4F5)
private val TextMain     = Color(0xFF0C200F)
private val TextMuted    = Color(0xFF5C7A63)

// ─── Type configs (colours swapped to palette) ────────────────────────────────
private data class TypeConfig(
    val type: TransactionType,
    val label: String,
    val emoji: String,
    val color: Color,
    val bgColor: Color
)

private val typeConfigs = listOf(
    TypeConfig(TransactionType.RECEIVED,       "Income",         "💰", GreenBrand, GreenPale),
    TypeConfig(TransactionType.SENT,           "Expense",        "💸", Rust,       RustPale),
    TypeConfig(TransactionType.SAVINGS,        "Savings",        "🏦", Teal,       TealPale),
    TypeConfig(TransactionType.LOAN_REPAYMENT, "Loan Repayment", "✅", Plum,       PlumPale)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    onSaveTransaction: (Transaction) -> Unit = {}
) {
    var selectedType  by remember { mutableStateOf(TransactionType.RECEIVED) }
    var amount        by remember { mutableStateOf("") }
    var description   by remember { mutableStateOf("") }
    var category      by remember { mutableStateOf("") }
    var selectedDate  by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var saving         by remember { mutableStateOf(false) }

    val activeConfig = typeConfigs.first { it.type == selectedType }

    // ── Date Picker ───────────────────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("Select", fontWeight = FontWeight.Bold, color = GreenBrand) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
    ) {

        // ── HEADER ────────────────────────────────────────────────────────────
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(GreenDeep, GreenMid)))
        ) {
            // Decorative blob
            Box(
                Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-20).dp)
                    .background(GreenAccent.copy(alpha = 0.07f), CircleShape)
            )

            Column(Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable { navController.popBackStack() },
                        Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowBackIosNew, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Add Transaction", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            "Record income, expense, savings or repayment",
                            fontSize = 12.sp, color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Active type summary pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.13f)
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(activeConfig.emoji, fontSize = 22.sp)
                        Column {
                            Text(
                                activeConfig.label,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                if (amount.isBlank()) "Enter amount below" else "KSh $amount",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Box(
                            Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(GreenAccent)
                        )
                    }
                }
            }
        }

        // ── BODY ──────────────────────────────────────────────────────────────
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Type Selector
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(CreamCard),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            Modifier.size(6.dp).clip(CircleShape).background(GreenBrand)
                        )
                        Text(
                            "Transaction Type",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMain
                        )
                    }
                    Spacer(Modifier.height(14.dp))
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
                    Spacer(Modifier.height(10.dp))
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

            // Form Fields
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(CreamCard),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(GreenBrand))
                        Text("Details", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
                    }

                    // Amount
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Amount (KSh)", color = TextMuted) },
                        placeholder = { Text("e.g. 5000", color = TextMuted.copy(alpha = 0.5f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Text(activeConfig.emoji, fontSize = 18.sp, modifier = Modifier.padding(start = 4.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeConfig.color,
                            unfocusedBorderColor = CreamBorder,
                            focusedLabelColor = activeConfig.color,
                            cursorColor = activeConfig.color,
                            unfocusedContainerColor = Cream,
                            focusedContainerColor = activeConfig.bgColor.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Description", color = TextMuted) },
                        placeholder = { Text("Salary, groceries, etc.", color = TextMuted.copy(alpha = 0.5f)) },
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenBrand,
                            unfocusedBorderColor = CreamBorder,
                            focusedLabelColor = GreenBrand,
                            cursorColor = GreenBrand,
                            unfocusedContainerColor = Cream,
                            focusedContainerColor = GreenSurface
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Category
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Category (optional)", color = TextMuted) },
                        placeholder = { Text("e.g. salary, shopping", color = TextMuted.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenBrand,
                            unfocusedBorderColor = CreamBorder,
                            focusedLabelColor = GreenBrand,
                            cursorColor = GreenBrand,
                            unfocusedContainerColor = Cream,
                            focusedContainerColor = GreenSurface
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Date
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Date", color = TextMuted) },
                        readOnly = true,
                        trailingIcon = {
                            Box(
                                Modifier
                                    .padding(end = 8.dp)
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(activeConfig.bgColor)
                                    .clickable { showDatePicker = true },
                                Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    null,
                                    tint = activeConfig.color,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenBrand,
                            unfocusedBorderColor = CreamBorder,
                            unfocusedContainerColor = Cream,
                            focusedContainerColor = GreenSurface
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            // ── Save Button ───────────────────────────────────────────────────
            Button(
                onClick = {
                    if (amount.isBlank()) return@Button
                    saving = true
                    val transaction = Transaction(
                        type        = selectedType,
                        amount      = amount.toDoubleOrNull() ?: 0.0,
                        date        = selectedDate,
                        description = description.ifBlank { null },
                        category    = if (category.isBlank()) null else category
                    )
                    onSaveTransaction(transaction)
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = !saving && amount.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = activeConfig.color,
                    disabledContainerColor = activeConfig.color.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Saving…", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                } else {
                    Text(activeConfig.emoji + "  Save ${activeConfig.label}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─── Type Chip ────────────────────────────────────────────────────────────────
@Composable
private fun TypeChip(
    config: TypeConfig,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) config.color else config.bgColor)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = config.color.copy(alpha = 0.25f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(config.emoji, fontSize = 22.sp)
            Text(
                config.label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else config.color
            )
            if (isSelected) {
                Box(Modifier.size(5.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)))
            }
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddTransactionScreenPreview() {
    HustleScoreTheme { AddTransactionScreen(navController = rememberNavController()) }
}
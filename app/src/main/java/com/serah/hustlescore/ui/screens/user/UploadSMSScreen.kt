package com.serah.hustlescore.ui.screens.user

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.serah.hustlescore.data.NotificationHelper
import com.serah.hustlescore.data.algorithm.HustleScoreEngine
import com.serah.hustlescore.models.Transaction
import com.serah.hustlescore.models.TransactionType
import com.serah.hustlescore.navigation.Routes
import com.serah.hustlescore.ui.theme.ThemeViewModel

private val HustleGreen = Color(0xFF1A7A3C)
private val DarkBg      = Color(0xFF121212)
private val DarkCard    = Color(0xFF1E1E1E)

@Composable
fun UploadSMSScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    val isDarkMode    by themeViewModel.isDarkMode.collectAsState()
    val backgroundColor = if (isDarkMode) DarkBg    else Color(0xFFF4F6F9)
    val cardColor       = if (isDarkMode) DarkCard   else Color.White
    val primaryText     = if (isDarkMode) Color.White else Color(0xFF111827)
    val secondaryText   = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val guideCardBg     = if (isDarkMode) Color(0xFF1A2E20) else Color(0xFFF0FDF4)
    val tipBg           = if (isDarkMode) Color(0xFF2A2010) else Color(0xFFFFFBEB)
    val tipText         = if (isDarkMode) Color(0xFFD4A853) else Color(0xFF92400E)

    var smsText            by remember { mutableStateOf("") }
    var parsedTransactions by remember { mutableStateOf<List<Transaction>?>(null) }
    var loading            by remember { mutableStateOf(false) }
    var showGuide          by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    val sampleSms = """OKX12345 Confirmed. You have received Ksh5000 from JOHN DOE on 01/03/2024 at 9:00 AM.
ABX98765 Confirmed. Ksh1500 sent to SUPERMARKET NAIROBI on 02/03/2024 at 11:30 AM.
CDX11111 Confirmed. Ksh2000 deposited to KCB SAVINGS on 05/03/2024 at 2:00 PM.
EFX22222 Confirmed. You have received Ksh8000 from EMPLOYER LTD on 07/03/2024 at 8:00 AM.
GHX33333 Confirmed. Ksh3000 paid to KPLC PREPAID on 10/03/2024 at 5:00 PM."""

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundColor).verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Upload M-Pesa SMS", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = primaryText)
        Text("Paste your M-Pesa messages to calculate your HustleScore", fontSize = 13.sp, color = secondaryText)

        // ── Guide Card ────────────────────────────────────────────────────────
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = guideCardBg)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showGuide = !showGuide },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhoneAndroid, null, tint = HustleGreen, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("How to export M-Pesa SMS", color = HustleGreen, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    Icon(if (showGuide) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = HustleGreen)
                }

                if (showGuide) {
                    Spacer(Modifier.height(12.dp))
                    listOf(
                        "1" to ("Open M-Pesa App" to "Go to M-Pesa → Statements → Request Statement"),
                        "2" to ("Select Period"   to "Choose up to 6 months recommended"),
                        "3" to ("Export File"     to "Export SMS or text file to phone storage"),
                        "4" to ("Paste Below"     to "Copy all content and paste it below")
                    ).forEach { (num, info) ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Box(Modifier.size(28.dp).clip(RoundedCornerShape(14.dp)).background(HustleGreen), Alignment.Center) {
                                Text(num, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(info.first,  fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = primaryText)
                                Text(info.second, fontSize = 11.sp, color = secondaryText)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = tipBg) {
                        Text("💡 Alternative: Forward M-Pesa SMS messages to a note app, then copy and paste here.",
                            modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = tipText)
                    }
                }
            }
        }

        // ── Input Card ────────────────────────────────────────────────────────
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = smsText,
                    onValueChange = { smsText = it },
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    placeholder = { Text("Paste your M-Pesa SMS messages here...", fontSize = 12.sp, color = secondaryText) },
                    label = { Text("M-Pesa SMS Text", color = secondaryText) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = HustleGreen,
                        unfocusedBorderColor = if (isDarkMode) Color(0xFF374151) else Color(0xFFD1D5DB),
                        focusedTextColor     = primaryText,
                        unfocusedTextColor   = primaryText,
                        focusedContainerColor    = if (isDarkMode) Color(0xFF1A2E20) else Color(0xFFF0FDF4),
                        unfocusedContainerColor  = cardColor
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { smsText = sampleSms }) {
                        Text("Load Sample Data", fontSize = 12.sp, color = HustleGreen)
                    }
                    if (smsText.isNotEmpty()) {
                        TextButton(onClick = { smsText = ""; parsedTransactions = null }) {
                            Icon(Icons.Default.Clear, null, modifier = Modifier.size(14.dp), tint = secondaryText)
                            Text(" Clear", fontSize = 12.sp, color = secondaryText)
                        }
                    }
                }
                Button(
                    onClick = { parsedTransactions = HustleScoreEngine.parseMpesaSms(smsText) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HustleGreen),
                    shape = RoundedCornerShape(12.dp),
                    enabled = smsText.isNotBlank()
                ) {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Parse Transactions")
                }
            }
        }

        // ── Results ───────────────────────────────────────────────────────────
        parsedTransactions?.let { parsed ->
            val hasResults = parsed.isNotEmpty()
            val resultBg     = if (hasResults) (if (isDarkMode) Color(0xFF1A2E20) else Color(0xFFF0FDF4)) else (if (isDarkMode) Color(0xFF3A1A1A) else Color(0xFFFEF2F2))
            val resultBorder = if (hasResults) (if (isDarkMode) Color(0xFF2E6040) else Color(0xFF86EFAC)) else (if (isDarkMode) Color(0xFF7A3030) else Color(0xFFFCA5A5))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = resultBg),
                border = BorderStroke(1.5.dp, resultBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (hasResults) Icons.Default.CheckCircle else Icons.Default.Error, null,
                            tint = if (hasResults) Color(0xFF16A34A) else Color(0xFFDC2626))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(if (hasResults) "${parsed.size} transactions found!" else "No transactions found",
                                fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = primaryText)
                            if (hasResults) {
                                Text("${parsed.count { it.type == TransactionType.INCOME }} income · ${parsed.count { it.type == TransactionType.EXPENSE }} expenses · ${parsed.count { it.type == TransactionType.SAVINGS }} savings",
                                    fontSize = 11.sp, color = secondaryText)
                            }
                        }
                    }

                    if (hasResults) {
                        Spacer(Modifier.height(12.dp))
                        parsed.take(5).forEach { tx ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (tx.type) {
                                        TransactionType.INCOME  -> if (isDarkMode) Color(0xFF1A3A25) else Color(0xFFDCFCE7)
                                        TransactionType.SAVINGS -> if (isDarkMode) Color(0xFF1A2A3A) else Color(0xFFDBEAFE)
                                        else                    -> if (isDarkMode) Color(0xFF3A1A1A) else Color(0xFFFEE2E2)
                                    }
                                ) {
                                    Text(tx.type.name.lowercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp,
                                        color = when (tx.type) {
                                            TransactionType.INCOME  -> if (isDarkMode) Color(0xFF4FCB78) else Color(0xFF15803D)
                                            TransactionType.SAVINGS -> if (isDarkMode) Color(0xFF60A5FA) else Color(0xFF1D4ED8)
                                            else                    -> Color(0xFFDC2626)
                                        })
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(tx.description.toString(), modifier = Modifier.weight(1f), fontSize = 12.sp, maxLines = 1, color = primaryText)
                                Text("KSh ${String.format("%,.0f", tx.amount)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = primaryText)
                            }
                        }
                        if (parsed.size > 5) {
                            Text("+${parsed.size - 5} more...", fontSize = 11.sp, color = secondaryText, modifier = Modifier.padding(top = 4.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                loading = true
                                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run { loading = false; return@Button }
                                val db       = FirebaseDatabase.getInstance().getReference("transactions/$uid")
                                val scoreRef = FirebaseDatabase.getInstance().getReference("scores/$uid")
                                var tasks    = parsed.map { tx -> db.push().setValue(tx.copy(UserId = uid)) }
                                val score    = HustleScoreEngine.calculate(parsed)
                                tasks += scoreRef.push().setValue(score)
                                com.google.android.gms.tasks.Tasks.whenAllComplete(tasks)
                                    .addOnSuccessListener {
                                        loading = false
                                        NotificationHelper.scoreCalculated(uid, score)
                                        NotificationHelper.lowScoreAlert(uid, score.totalScore)
                                        navController.navigate(Routes.ScoreBreakdown.route)
                                    }
                                    .addOnFailureListener { e ->
                                        loading = false
                                        Log.e("FirebaseSave", "Failed to save data", e)
                                    }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = HustleGreen),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !loading
                        ) {
                            if (loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Save & Calculate HustleScore")
                            }
                        }
                    }
                }
            }
        }
    }
}
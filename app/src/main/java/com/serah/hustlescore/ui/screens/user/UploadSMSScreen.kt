package com.serah.hustlescore.ui.screens.user

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.hustlescore.ui.theme.HustleScoreTheme
import com.serah.hustlescore.data.algorithm.HustleScoreEngine
import com.serah.hustlescore.models.Transaction
import com.serah.hustlescore.models.TransactionType
import com.serah.hustlescore.navigation.Routes
import com.serah.hustlescore.ui.theme.BackgroundGray
import com.serah.hustlescore.ui.theme.HustleGreen
import com.serah.hustlescore.ui.theme.TextSecondary

@Composable
fun UploadSMSScreen(navController: NavController)  {
    var smsText by remember { mutableStateOf("") }
    var parsedTransactions by remember { mutableStateOf<List<Transaction>?>(null) }
    var loading by remember { mutableStateOf(false) }
    var showGuide by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    val sampleSms = """OKX12345 Confirmed. You have received Ksh5000 from JOHN DOE on 01/03/2024 at 9:00 AM.
ABX98765 Confirmed. Ksh1500 sent to SUPERMARKET NAIROBI on 02/03/2024 at 11:30 AM.
CDX11111 Confirmed. Ksh2000 deposited to KCB SAVINGS on 05/03/2024 at 2:00 PM.
EFX22222 Confirmed. You have received Ksh8000 from EMPLOYER LTD on 07/03/2024 at 8:00 AM.
GHX33333 Confirmed. Ksh3000 paid to KPLC PREPAID on 10/03/2024 at 5:00 PM."""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Upload M-Pesa SMS",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Paste your M-Pesa messages to calculate your HustleScore",
            fontSize = 13.sp,
            color = TextSecondary
        )

        // Guide Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGuide = !showGuide },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = HustleGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "How to export M-Pesa SMS",
                            color = HustleGreen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                    Icon(
                        imageVector = if (showGuide) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = HustleGreen
                    )
                }

                if (showGuide) {
                    Spacer(modifier = Modifier.height(12.dp))

                    val steps = listOf(
                        "1" to Pair("Open M-Pesa App", "Go to M-Pesa → Statements → Request Statement"),
                        "2" to Pair("Select Period", "Choose up to 6 months recommended"),
                        "3" to Pair("Export File", "Export SMS or text file to phone storage"),
                        "4" to Pair("Paste Below", "Copy all content and paste it below")
                    )

                    steps.forEach { (num, info) ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(HustleGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = num,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = info.first,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = info.second,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFFBEB)
                    ) {
                        Text(
                            text = "💡 Alternative: Forward M-Pesa SMS messages to a note app, then copy and paste here.",
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }
        }

        // Input Card
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = smsText,
                    onValueChange = { smsText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    placeholder = {
                        Text(
                            text = "Paste your M-Pesa SMS messages here...",
                            fontSize = 12.sp
                        )
                    },
                    label = { Text(text = "M-Pesa SMS Text") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { smsText = sampleSms }) {
                        Text(
                            text = "Load Sample Data",
                            fontSize = 12.sp,
                            color = HustleGreen
                        )
                    }
                    if (smsText.isNotEmpty()) {
                        TextButton(onClick = {
                            smsText = ""
                            parsedTransactions = null
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = " Clear",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        parsedTransactions = HustleScoreEngine.parseMpesaSms(smsText)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HustleGreen),
                    shape = RoundedCornerShape(12.dp),
                    enabled = smsText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Parse Transactions")
                }
            }
        }

        // Results
        parsedTransactions?.let { parsed ->
            val hasResults = parsed.isNotEmpty()

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasResults) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                ),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (hasResults) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (hasResults) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (hasResults) Color(0xFF16A34A) else Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (hasResults) "${parsed.size} transactions found!" else "No transactions found",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            if (hasResults) {
                                Text(
                                    text = "${parsed.count { it.type == TransactionType.INCOME }} income · " +
                                            "${parsed.count { it.type == TransactionType.EXPENSE }} expenses · " +
                                            "${parsed.count { it.type == TransactionType.SAVINGS }} savings",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    if (hasResults) {
                        Spacer(modifier = Modifier.height(12.dp))

                        parsed.take(5).forEach { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (tx.type) {
                                        TransactionType.INCOME -> Color(0xFFDCFCE7)
                                        TransactionType.SAVINGS -> Color(0xFFDBEAFE)
                                        else -> Color(0xFFFEE2E2)
                                    }
                                ) {
                                    Text(
                                        text = tx.type.name.lowercase(),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontSize = 10.sp,
                                        color = when (tx.type) {
                                            TransactionType.INCOME -> Color(0xFF15803D)
                                            TransactionType.SAVINGS -> Color(0xFF1D4ED8)
                                            else -> Color(0xFFDC2626)
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tx.description.toString(),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = "KSh ${String.format("%,.0f", tx.amount)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (parsed.size > 5) {
                            Text(
                                text = "+${parsed.size - 5} more...",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                loading = true
                                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                                    loading = false
                                    return@Button
                                }

                                val db = FirebaseDatabase.getInstance().getReference("transactions/$uid")
                                val scoreRef = FirebaseDatabase.getInstance().getReference("scores/$uid")

                                // Save all transactions
                                var tasks = parsed.map { tx ->
                                    db.push().setValue(tx.copy(UserId = uid))
                                }

                                // Save score
                                val score = HustleScoreEngine.calculate(parsed)
                                tasks += scoreRef.push().setValue(score)

                                // Wait for all writes to complete
                                com.google.android.gms.tasks.Tasks.whenAllComplete(tasks)
                                    .addOnSuccessListener {
                                        loading = false
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
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Save & Calculate HustleScore")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UploadSMSScreenPreview() {
    HustleScoreTheme {   // Replace with your actual Theme name if different
        UploadSMSScreen(navController = rememberNavController())
    }
}










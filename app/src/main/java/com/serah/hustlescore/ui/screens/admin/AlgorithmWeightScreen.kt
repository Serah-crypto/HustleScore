package com.serah.hustlescore.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.database.*
import com.serah.hustlescore.components.ScoreGauge

// Update these imports to match your actual model locations
import com.serah.hustlescore.data.algorithm.HustleScoreEngine
import com.serah.hustlescore.models.AlgorithmWeights
import com.serah.hustlescore.models.Transaction
import com.serah.hustlescore.models.TransactionType
import com.serah.hustlescore.ui.theme.BackgroundGray
import com.serah.hustlescore.ui.theme.HustleGreen
import com.serah.hustlescore.ui.theme.TextSecondary

private val SAMPLE_TXS = listOf(
    Transaction(amount = 25000.0, type = TransactionType.INCOME, date = "2024-01-10",),
    Transaction(amount = 28000.0, type = TransactionType.INCOME, date = "2024-02-10",),
    Transaction(amount = 8000.0, date = "2024-01-15",),
    Transaction(amount = 5000.0, type = TransactionType.SAVINGS, date = "2024-01-20",),
    Transaction(amount = 3000.0, type = TransactionType.LOAN_REPAYMENT, date = "2024-01-25",),
)

@Composable
fun AlgorithmWeightsScreen(navController: NavController) {
    var incomeW by remember { mutableFloatStateOf(30f) }
    var savingsW by remember { mutableFloatStateOf(25f) }
    var expenseW by remember { mutableFloatStateOf(20f) }
    var activityW by remember { mutableFloatStateOf(15f) }
    var debtW by remember { mutableFloatStateOf(10f) }
    var version by remember { mutableStateOf("v1.0") }
    var notes by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var historyList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    val total = incomeW + savingsW + expenseW + activityW + debtW
    val isValid = kotlin.math.abs(total - 100f) < 1f

    val currentWeights = AlgorithmWeights(
        (incomeW / 100f).toDouble(),
        (savingsW / 100f).toDouble(),
        (expenseW / 100f).toDouble(),
        (activityW / 100f).toDouble(),
        (debtW / 100f).toDouble()
    )
    val previewScore = HustleScoreEngine.calculate(SAMPLE_TXS, currentWeights)

    LaunchedEffect(Unit) {
        FirebaseDatabase.getInstance().getReference("algorithm_weights")
            .orderByChild("timestamp").limitToLast(5)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    historyList = snapshot.children.mapNotNull {
                        @Suppress("UNCHECKED_CAST")
                        it.value as? Map<String, Any>
                    }.reversed()
                }
                override fun onCancelled(e: DatabaseError) {}
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Algorithm Weights", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Adjust the scoring formula and preview impact", fontSize = 13.sp, color = TextSecondary)

        // Live Preview
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(linearGradient(listOf(HustleGreen, Color(0xFF145A32))))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("LIVE PREVIEW", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    ScoreGauge(
                        score = previewScore.totalScore,
                        size = 120.dp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("Sample score with current weights", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        }

        // Weight Controls
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Scoring Weights", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Surface(shape = RoundedCornerShape(10.dp), color = if (isValid) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)) {
                        Text("Total: ${total.toInt()}%", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = if (isValid) Color(0xFF16A34A) else Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))

                val weightItems = listOf(
                    "💰 Income Stability" to incomeW to { v: Float -> incomeW = v },
                    "🏦 Savings Ratio" to savingsW to { v: Float -> savingsW = v },
                    "📊 Expense Control" to expenseW to { v: Float -> expenseW = v },
                    "📱 Transaction Activity" to activityW to { v: Float -> activityW = v },
                    "✅ Debt Behavior" to debtW to { v: Float -> debtW = v },
                )

                weightItems.forEach { (data, setter) ->
                    val (label, value) = data
                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("${value.toInt()}%", fontSize = 13.sp, color = HustleGreen, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(6.dp))
                        Slider(
                            value = value,
                            onValueChange = setter,
                            valueRange = 5f..60f,
                            steps = 10,
                            colors = SliderDefaults.colors(thumbColor = HustleGreen, activeTrackColor = HustleGreen)
                        )
                    }
                }
            }
        }

        // Version & Save
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = version, onValueChange = { version = it }, label = { Text("Version Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Change Notes") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Button(
                    onClick = {
                        if (!isValid) return@Button
                        saving = true
                        val db = FirebaseDatabase.getInstance().getReference("algorithm_weights")
                        val weightData = mapOf(
                            "version" to version,
                            "notes" to notes,
                            "incomeWeight" to incomeW / 100,
                            "savingsWeight" to savingsW / 100,
                            "expenseWeight" to expenseW / 100,
                            "activityWeight" to activityW / 100,
                            "debtWeight" to debtW / 100,
                            "timestamp" to System.currentTimeMillis(),
                            "isActive" to true
                        )
                        db.push().setValue(weightData).addOnCompleteListener {
                            saving = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HustleGreen),
                    enabled = isValid && !saving
                ) {
                    if (saving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save & Activate Weights")
                    }
                }
            }
        }

        // History
        if (historyList.isNotEmpty()) {
            Text("Version History", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    historyList.forEach { h ->
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(h["version"]?.toString() ?: "v?", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(h["notes"]?.toString() ?: "", fontSize = 11.sp, color = TextSecondary)
                            }
                            if (h["isActive"] == true) {
                                Surface(shape = RoundedCornerShape(8.dp), color = HustleGreen.copy(alpha = 0.1f)) {
                                    Text("Active", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = HustleGreen, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlgorithmWeightScreenPreview(){
    AlgorithmWeightScreen(rememberNavController())



}

@Composable
fun AlgorithmWeightScreen(x0: NavHostController) {
    TODO("Not yet implemented")
}
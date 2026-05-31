package com.hustlescore.screens.user

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.serah.hustlescore.components.ScoreGauge
import com.serah.hustlescore.data.algorithm.HustleScoreEngine
import com.serah.hustlescore.models.HustleScore
import com.serah.hustlescore.models.Transaction
import com.serah.hustlescore.navigation.Routes
import com.serah.hustlescore.ui.theme.ThemeViewModel

data class ScoreFactor(val key: String, val label: String, val weight: String, val emoji: String, val value: Int)

@Composable
fun ScoreBreakdownScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()

    // ── Theme-aware colours ───────────────────────────────────────────────────
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF4F6F9)
    val cardColor       = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val primaryText     = if (isDarkMode) Color.White       else Color(0xFF111827)
    val secondaryText   = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val trackColor      = if (isDarkMode) Color(0xFF374151) else Color(0xFFE5E7EB)

    val HustleGreen = Color(0xFF1E8449)

    var scoreData by remember { mutableStateOf<HustleScore?>(null) }
    var loading   by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        FirebaseDatabase.getInstance().getReference("transactions/$uid")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val txs = snapshot.children.mapNotNull { it.getValue(Transaction::class.java) }
                    scoreData = if (txs.isNotEmpty()) HustleScoreEngine.calculate(txs) else null
                    loading = false
                }
                override fun onCancelled(error: DatabaseError) { loading = false }
            })
    }

    if (loading) {
        Box(Modifier.fillMaxSize().background(backgroundColor), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = HustleGreen)
        }
        return
    }

    if (scoreData == null) {
        Box(Modifier.fillMaxSize().background(backgroundColor).padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📊", fontSize = 48.sp)
                Spacer(Modifier.height(16.dp))
                Text("No Score Yet", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = primaryText)
                Text("Upload M-Pesa SMS first", color = secondaryText, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { navController.navigate(Routes.UploadSms.route) },
                    colors = ButtonDefaults.buttonColors(containerColor = HustleGreen)) {
                    Text("Upload SMS")
                }
            }
        }
        return
    }

    val score = scoreData!!
    val factors = listOf(
        ScoreFactor("income",   "Income Stability",      "30%", "💰", score.incomeScore),
        ScoreFactor("savings",  "Savings Ratio",         "25%", "🏦", score.savingsScore),
        ScoreFactor("expense",  "Expense Control",       "20%", "📊", score.expenseScore),
        ScoreFactor("activity", "Transaction Activity",  "15%", "📱", score.activityScore),
        ScoreFactor("debt",     "Debt Behavior",         "10%", "✅", score.debtScore),
    )

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundColor)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Score Breakdown", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = primaryText)
        Text("Understand what drives your HustleScore", fontSize = 13.sp, color = secondaryText)

        // Main Score Card
        Card(shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(8.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.linearGradient(listOf(HustleGreen, Color(0xFF145A32))))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ScoreGauge(score = score.totalScore, size = 160.dp)
                    Spacer(Modifier.height(12.dp))
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f)) {
                        Text("${score.grade.label} Credit Profile",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Factor Cards
        factors.forEach { factor ->
            val animProgress = remember { Animatable(0f) }
            LaunchedEffect(factor.value) {
                animProgress.animateTo(factor.value / 1000f, tween(800, easing = FastOutSlowInEasing))
            }
            val barColor = HustleScoreEngine.getScoreColor(factor.value)

            Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(factor.emoji, fontSize = 24.sp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(factor.label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = primaryText)
                                Text("Weight: ${factor.weight}", fontSize = 11.sp, color = secondaryText)
                            }
                        }
                        Text("${factor.value}", color = barColor, fontWeight = FontWeight.Black, fontSize = 24.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(trackColor)) {
                        Box(Modifier.fillMaxWidth(animProgress.value).height(8.dp).clip(RoundedCornerShape(4.dp)).background(barColor))
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("0", fontSize = 10.sp, color = secondaryText)
                        Text("${(factor.value / 10)}%", fontSize = 10.sp, color = barColor, fontWeight = FontWeight.SemiBold)
                        Text("1000", fontSize = 10.sp, color = secondaryText)
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Routes.CreditReport.route) }) {
                Text("Download Report", color = primaryText)
            }
            Button(modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Routes.FinancialAdvice.route) },
                colors = ButtonDefaults.buttonColors(containerColor = HustleGreen)) {
                Text("Get Advice")
            }
        }
    }
}
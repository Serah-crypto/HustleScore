package com.serah.hustlescore.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.serah.hustlescore.components.ScoreGauge
import com.serah.hustlescore.data.algorithm.HustleScoreEngine
import com.serah.hustlescore.models.AppUser
import com.serah.hustlescore.models.HustleScore
import com.serah.hustlescore.models.Transaction
import com.serah.hustlescore.models.TransactionType
import com.serah.hustlescore.ui.theme.BackgroundGray
import com.serah.hustlescore.ui.theme.HustleGreen
import com.serah.hustlescore.ui.theme.TextSecondary

@Composable
fun UserDetailScreen(navController: NavController, userId: String) {
    var user by remember { mutableStateOf<AppUser?>(null) }
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var scoreData by remember { mutableStateOf<HustleScore?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        FirebaseDatabase.getInstance().getReference("users/$userId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(AppUser::class.java)?.copy(id = userId)
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        FirebaseDatabase.getInstance().getReference("transactions/$userId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val txs = snapshot.children.mapNotNull { it.getValue(Transaction::class.java) }
                    transactions = txs
                    scoreData = if (txs.isNotEmpty()) HustleScoreEngine.calculate(txs) else null
                    loading = false
                }
                override fun onCancelled(error: DatabaseError) {
                    loading = false
                }
            })
    }

    if (loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = HustleGreen)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = user?.fullName ?: "User Detail",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user?.email ?: "",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (scoreData != null) {
                val score = scoreData!!

                // Score Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(HustleGreen, Color(0xFF145A32))
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ScoreGauge(
                                    score = score.totalScore,
                                    size = 100.dp,
                                    strokeWidth = 12f
                                )
                                Spacer(modifier = Modifier.width(20.dp))
                                Column {
                                    Text(
                                        text = "${score.totalScore}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 36.sp
                                    )
                                    Text(
                                        text = "HustleScore",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 13.sp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = score.grade.label,
                                            modifier = Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 4.dp
                                            ),
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Summary Row
                        val totalIncome = transactions
                            .filter { it.type == TransactionType.INCOME }
                            .sumOf { it.amount }
                        val totalExpenses = transactions
                            .filter { it.type == TransactionType.EXPENSE }
                            .sumOf { it.amount }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            SummaryItem(
                                label = "Income",
                                value = "KSh ${(totalIncome / 1000).toInt()}k",
                                color = Color(0xFF16A34A)
                            )
                            SummaryItem(
                                label = "Expenses",
                                value = "KSh ${(totalExpenses / 1000).toInt()}k",
                                color = Color(0xFFDC2626)
                            )
                            SummaryItem(
                                label = "Txns",
                                value = "${transactions.size}",
                                color = Color(0xFF7C3AED)
                            )
                        }
                    }
                }

                // Factor Bars Card
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Score Factors",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val factors = listOf(
                            "Income Stability" to score.incomeScore,
                            "Savings Ratio" to score.savingsScore,
                            "Expense Control" to score.expenseScore,
                            "Transaction Activity" to score.activityScore,
                            "Debt Behavior" to score.debtScore
                        )

                        factors.forEach { (label, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.width(120.dp),
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFFE5E7EB))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(value / 1000f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(HustleScoreEngine.getScoreColor(value))
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$value",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HustleScoreEngine.getScoreColor(score.totalScore)
                                )
                            }
                        }
                    }
                }

            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, Color(0xFFE5E7EB))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No score data available.",
                            color = TextSecondary
                        )
                    }
                }
            }

            // Recent Transactions Card
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Recent Transactions (${transactions.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    transactions.take(10).forEach { tx ->
                        val isIncome = tx.type == TransactionType.INCOME
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isIncome) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isIncome) "↑" else "↓",
                                    color = if (isIncome) Color(0xFF16A34A) else Color(0xFFDC2626),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.description.ifBlank { "M-Pesa" },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                Text(
                                    text = tx.date,
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "${if (isIncome) "+" else "-"}KSh ${
                                    String.format("%,.0f", tx.amount)
                                }",
                                color = if (isIncome) Color(0xFF16A34A) else Color(0xFFDC2626),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Reusable summary stat composable
@Composable
fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}
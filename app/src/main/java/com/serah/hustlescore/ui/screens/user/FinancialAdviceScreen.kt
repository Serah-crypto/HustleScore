package com.serah.hustlescore.ui.screens.user

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.serah.hustlescore.data.algorithm.HustleScoreEngine
import com.serah.hustlescore.models.FinancialAdvice
import com.serah.hustlescore.models.Priority
import com.serah.hustlescore.models.Transaction
import com.serah.hustlescore.ui.theme.BackgroundGray
import com.serah.hustlescore.ui.theme.HustleGreen
import com.serah.hustlescore.ui.theme.TextSecondary

@Composable
fun FinancialAdviceScreen(navController: NavController) {
    var advice by remember { mutableStateOf<List<FinancialAdvice>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        FirebaseDatabase.getInstance().getReference("transactions/$uid")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val transactions: List<Transaction> = snapshot.children.mapNotNull {
                        it.getValue(Transaction::class.java)
                    }
                    if (transactions.isNotEmpty()) {
                        val score = HustleScoreEngine.calculate(transactions)
                        advice = HustleScoreEngine.getAdvice(score)
                    }
                    loading = false
                }
                override fun onCancelled(error: DatabaseError) {
                    loading = false
                }
            })
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = HustleGreen)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Financial Advice", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Personalized tips to boost your HustleScore", fontSize = 13.sp, color = TextSecondary)

        // Gold Banner
        Card(shape = RoundedCornerShape(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", fontSize = 20.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "AI-Powered Insights",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "Based on your M-Pesa activity",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        if (advice.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("💡", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No advice yet", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Upload your M-Pesa SMS to get personalized advice.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            advice.forEach { tip ->
                val priorityColor = when (tip.priority) {
                    Priority.HIGH   -> Color(0xFFDC2626)
                    Priority.MEDIUM -> Color(0xFFF59E0B)
                    Priority.LOW    -> Color(0xFF16A34A)
                }
                val priorityLabel = when (tip.priority) {
                    Priority.HIGH   -> "High Priority"
                    Priority.MEDIUM -> "Medium"
                    Priority.LOW    -> "Tip"
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(HustleGreen.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(tip.icon, fontSize = 24.sp)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    tip.title,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = priorityColor.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        priorityLabel,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        color = priorityColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                tip.description,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Kenyan Resources
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "🇰🇪 Kenyan Financial Resources",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = HustleGreen
                )
                Spacer(Modifier.height(10.dp))
                listOf(
                    "M-Shwari"   to "KCB savings & micro-loans via M-Pesa",
                    "KCB M-Pesa" to "Higher credit limits, up to KSh 1M",
                    "Fuliza"     to "Overdraft facility on M-Pesa",
                    "Equity TING" to "Group savings and loan products",
                    "SACCO"      to "Join a co-op for affordable credit"
                ).forEach { (name, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(HustleGreen)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(6.dp))
                        Text(desc, fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}
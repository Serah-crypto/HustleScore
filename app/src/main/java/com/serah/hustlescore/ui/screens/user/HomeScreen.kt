package com.serah.hustlescore.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hustlescore.ui.theme.HustleScoreTheme
import com.serah.hustlescore.data.algorithm.HustleScoreEngine
import com.serah.hustlescore.models.HustleScore
import com.serah.hustlescore.models.Transaction
import com.serah.hustlescore.models.TransactionType
import com.serah.hustlescore.navigation.Routes
import com.serah.hustlescore.ui.theme.*

@Suppress("UNCHECKED_CAST")
private fun DataSnapshot.toTransaction(): Transaction? = try {
    val map = value as? Map<String, Any?> ?: return null
    val typeStr = (map["typeRaw"] as? String) ?: (map["type"] as? String) ?: "INCOME"
    Transaction(
        type        = runCatching { TransactionType.valueOf(typeStr) }.getOrDefault(TransactionType.INCOME),
        UserId      = map["UserId"] as? String,
        id          = (map["id"] as? String) ?: key,
        amount      = (map["amount"] as? Number)?.toDouble() ?: 0.0,
        date        = map["date"] as? String ?: "",
        description = map["description"] as? String,
        category    = map["category"] as? String,
        phone       = map["phone"] as? String,
        name        = map["name"] as? String,
        mpesaRef    = map["mpesaRef"] as? String,
        rawSms      = map["rawSms"] as? String,
        balance     = (map["balance"] as? Number)?.toDouble(),
        timestamp   = (map["timestamp"] as? Number)?.toLong() ?: 0L
    )
} catch (e: Exception) {
    android.util.Log.e("HomeScreen", "toTransaction failed: ${e.message}")
    null
}

@Composable
fun HomeScreen(navController: NavController) {
    var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { currentUser = it.currentUser }
        FirebaseAuth.getInstance().addAuthStateListener(listener)
        onDispose { FirebaseAuth.getInstance().removeAuthStateListener(listener) }
    }

    val firstName = currentUser?.displayName?.split(" ")?.firstOrNull() ?: "there"
    val initials  = currentUser?.displayName?.split(" ")
        ?.mapNotNull { it.firstOrNull()?.toString() }?.take(2)?.joinToString("") ?: "U"

    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var scoreData    by remember { mutableStateOf<HustleScore?>(null) }
    var loading      by remember { mutableStateOf(true) }
    var isAdmin      by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid ?: return@LaunchedEffect

        FirebaseDatabase.getInstance().getReference("Users/$uid/role")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    val role = s.getValue(String::class.java)
                    android.util.Log.d("ADMIN_CHECK", "role=$role")
                    isAdmin = role == "admin"
                }
                override fun onCancelled(e: DatabaseError) {}
            })

        // ✅ Uses toTransaction() — no getValue(Transaction::class.java), no crash
        FirebaseDatabase.getInstance().getReference("transactions/$uid")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val txs = snapshot.children.mapNotNull { it.toTransaction() }
                    transactions = txs
                    scoreData = if (txs.isNotEmpty()) HustleScoreEngine.calculate(txs) else null
                    loading = false
                }
                override fun onCancelled(error: DatabaseError) { loading = false }
            })
    }

    val totalIncome   = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val score = scoreData
    val hour  = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when { hour < 12 -> "Good morning"; hour < 17 -> "Good afternoon"; else -> "Good evening" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundGray),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF0F4C2A), HustleGreen)))
                    .padding(horizontal = 20.dp, vertical = 28.dp)
            ) {
                Column {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.25f)), Alignment.Center) {
                                Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(greeting, color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                                Text(firstName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isAdmin) {
                                Box(
                                    Modifier.padding(end = 4.dp).clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.18f))
                                        .clickable { navController.navigate(Routes.AdminDashboard.route) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp), Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AdminPanelSettings, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(5.dp))
                                        Text("Admin", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            IconButton(onClick = { navController.navigate(Routes.UserDashboard.route) }) {
                                Icon(Icons.Default.Dashboard, null, tint = Color.White, modifier = Modifier.size(26.dp))
                            }
                            Box {
                                IconButton(onClick = { navController.navigate(Routes.Notifications.route) }) {
                                    Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(26.dp))
                                }
                                Badge(Modifier.align(Alignment.TopEnd), containerColor = Color(0xFFF59E0B)) {
                                    Text("3", fontSize = 9.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    if (score != null) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) {
                            Column {
                                Text("Your HustleScore", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                                Text("${score.totalScore}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 52.sp, lineHeight = 56.sp)
                                Text("out of 1000", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.2f)) {
                                    Text(score.grade.label, Modifier.padding(horizontal = 14.dp, vertical = 6.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("${transactions.size} transactions", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = { score.totalScore / 1000f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = Color.White, trackColor = Color.White.copy(alpha = 0.25f)
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Poor", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                            Text("Excellent", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        }
                    } else {
                        Text("No Score Yet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text("Upload your M-Pesa SMS to get your HustleScore", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                        Spacer(Modifier.height(14.dp))
                        Button(onClick = { navController.navigate(Routes.UploadSms.route) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.Upload, null, tint = HustleGreen, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Upload M-Pesa SMS", color = HustleGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), Arrangement.spacedBy(12.dp)) {
                MiniStatCard(Modifier.weight(1f), "Income",       "KSh ${(totalIncome/1000).toInt()}k",   Color(0xFF16A34A), Color(0xFFDCFCE7))
                MiniStatCard(Modifier.weight(1f), "Expenses",     "KSh ${(totalExpenses/1000).toInt()}k", Color(0xFFDC2626), Color(0xFFFEE2E2))
                MiniStatCard(Modifier.weight(1f), "Transactions", "${transactions.size}",                  Color(0xFF7C3AED), Color(0xFFEDE9FE))
            }
        }

        if (score != null) {
            item {
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { navController.navigate(Routes.UploadSms.route) },
                    RoundedCornerShape(16.dp), CardDefaults.cardColors(Color(0xFFF0FDF4)), CardDefaults.cardElevation(0.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(HustleGreen), Alignment.Center) {
                            Icon(Icons.Default.Upload, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Update Your Score", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                            Text("Upload new M-Pesa SMS to refresh your score", fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Icon(Icons.Default.ArrowForwardIos, null, tint = HustleGreen, modifier = Modifier.size(20.dp))
                    }
                }
            }
            item { Spacer(Modifier.height(4.dp)) }
        }

        if (score != null) {
            item {
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(Modifier.padding(18.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("Score Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            Text("View all →", fontSize = 12.sp, color = HustleGreen, fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { navController.navigate(Routes.ScoreBreakdown.route) })
                        }
                        Spacer(Modifier.height(14.dp))
                        listOf(
                            Triple("Income",   score.incomeScore,   Color(0xFF16A34A)),
                            Triple("Savings",  score.savingsScore,  Color(0xFF2563EB)),
                            Triple("Expenses", score.expenseScore,  Color(0xFFF59E0B)),
                            Triple("Activity", score.activityScore, Color(0xFF7C3AED)),
                            Triple("Debt",     score.debtScore,     Color(0xFFEA580C))
                        ).forEach { (label, value, color) ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(label, Modifier.width(70.dp), fontSize = 12.sp, color = TextSecondary)
                                Box(Modifier.weight(1f).height(7.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFE5E7EB))) {
                                    Box(Modifier.fillMaxWidth(value / 1000f).height(7.dp).clip(RoundedCornerShape(4.dp)).background(color))
                                }
                                Spacer(Modifier.width(10.dp))
                                Text("$value", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.width(36.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("What would you like to do?", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureTile(Modifier.weight(1f), "My Score",      "Full breakdown",          Icons.Default.TrendingUp,         Color(0xFF1D4ED8)) { navController.navigate(Routes.ScoreBreakdown.route) }
                    FeatureTile(Modifier.weight(1f), "Credit Report", "Download PDF",            Icons.Default.Description,        Color(0xFF7C3AED)) { navController.navigate(Routes.CreditReport.route) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureTile(Modifier.weight(1f), "Get Advice",    "Financial tips",          Icons.Default.Lightbulb,          Color(0xFFD97706)) { navController.navigate(Routes.FinancialAdvice.route) }
                    FeatureTile(Modifier.weight(1f), "Profile",       "Account settings",        Icons.Default.Person,             Color(0xFF0F766E)) { navController.navigate(Routes.UserProfile.route) }
                }
                if (isAdmin) {
                    FeatureTile(Modifier.fillMaxWidth(), "Admin Panel", "Manage platform & users", Icons.Default.AdminPanelSettings, Color(0xFFDC2626)) { navController.navigate(Routes.AdminDashboard.route) }
                }
            }
        }

        if (transactions.isNotEmpty()) {
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Recent Transactions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text("View all →", fontSize = 12.sp, color = HustleGreen, fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { navController.navigate(Routes.ScoreBreakdown.route) })
                }
            }
            item {
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(Modifier.padding(vertical = 8.dp)) {
                        transactions.take(5).forEachIndexed { index, tx ->
                            val isIncome = tx.type == TransactionType.INCOME
                            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                    .background(if (isIncome) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)), Alignment.Center) {
                                    Text(if (isIncome) "↑" else "↓",
                                        color = if (isIncome) Color(0xFF16A34A) else Color(0xFFDC2626),
                                        fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(tx.description.orEmpty().ifBlank { "M-Pesa Transaction" },
                                        fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(tx.date, fontSize = 11.sp, color = TextSecondary)
                                }
                                Text("${if (isIncome) "+" else "-"}KSh ${String.format("%,.0f", tx.amount)}",
                                    color = if (isIncome) Color(0xFF16A34A) else Color(0xFFDC2626),
                                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                            if (index < minOf(transactions.size, 5) - 1)
                                Box(Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 16.dp).background(Color(0xFFE5E7EB)))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatCard(modifier: Modifier = Modifier, label: String, value: String, valueColor: Color, bgColor: Color) {
    Card(modifier, RoundedCornerShape(14.dp), CardDefaults.cardColors(bgColor), CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Text(label, fontSize = 10.sp, color = valueColor.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun FeatureTile(modifier: Modifier = Modifier, title: String, subtitle: String, icon: ImageVector, bgColor: Color, onClick: () -> Unit) {
    Card(modifier.height(100.dp).clickable { onClick() }, RoundedCornerShape(18.dp), CardDefaults.cardColors(bgColor), CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.fillMaxSize().padding(14.dp), Arrangement.SpaceBetween) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(26.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HustleScoreTheme { HomeScreen(navController = rememberNavController()) }
}
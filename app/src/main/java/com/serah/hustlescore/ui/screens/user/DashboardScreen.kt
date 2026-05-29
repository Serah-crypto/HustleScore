package com.serah.hustlescore.ui.screens.user

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.serah.hustlescore.components.ScoreGauge
import com.serah.hustlescore.data.algorithm.HustleScoreEngine
import com.serah.hustlescore.models.HustleScore
import com.serah.hustlescore.models.Transaction
import com.serah.hustlescore.models.TransactionType
import com.serah.hustlescore.navigation.Routes

// ─── Palette ─────────────────────────────────────────────────────────────────
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

@Composable
fun DashboardScreen(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var scoreData    by remember { mutableStateOf<HustleScore?>(null) }
    var loading      by remember { mutableStateOf(true) }
    val firstName = currentUser?.displayName?.split(" ")?.firstOrNull() ?: "there"
    val initials  = currentUser?.displayName?.split(" ")
        ?.mapNotNull { it.firstOrNull()?.toString() }?.take(2)?.joinToString("") ?: "U"

    LaunchedEffect(Unit) {
        val uid = currentUser?.uid ?: return@LaunchedEffect
        FirebaseDatabase.getInstance().getReference("transactions/$uid")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val txs = snapshot.children.mapNotNull { it.getValue(Transaction::class.java) }
                    transactions = txs
                    scoreData = if (txs.isNotEmpty()) HustleScoreEngine.calculate(txs) else null
                    loading = false
                }
                override fun onCancelled(error: DatabaseError) { loading = false }
            })
    }

    val totalIncome   = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val totalSavings  = transactions.filter { it.type == TransactionType.SAVINGS }.sumOf { it.amount }

    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when { hour < 12 -> "Good morning"; hour < 17 -> "Good afternoon"; else -> "Good evening" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Cream),
        contentPadding = PaddingValues(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {

        // ── HEADER ───────────────────────────────────────────────────────────
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(GreenDeep, GreenMid, GreenBrand)))
            ) {
                // Decorative blobs
                Box(Modifier.size(160.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-30).dp)
                    .background(GreenAccent.copy(alpha = 0.07f), CircleShape))
                Box(Modifier.size(90.dp).align(Alignment.BottomStart).offset(x = (-20).dp, y = 20.dp)
                    .background(Color.White.copy(alpha = 0.04f), CircleShape))

                Column(Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                    // Top row
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                Modifier.size(44.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(GreenAccent, GreenLight))),
                                Alignment.Center
                            ) {
                                Text(initials, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                            }
                            Column {
                                Text("$greeting, $firstName 👋", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Here's your financial overview", fontSize = 12.sp, color = Color.White.copy(alpha = 0.65f))
                            }
                        }
                        // Upload SMS button
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable {
                                    navController.navigate(Routes.UserDashboard.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Icon(Icons.Default.Upload, null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Text("Upload SMS", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Score gauge card ──────────────────────────────────────
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.10f)
                    ) {
                        if (scoreData != null) {
                            val score = scoreData!!
                            Column(
                                Modifier.fillMaxWidth().padding(vertical = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                ScoreGauge(score = score.totalScore, size = 150.dp)
                                Spacer(Modifier.height(12.dp))
                                Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.18f)) {
                                    Text(
                                        score.grade.label,
                                        Modifier.padding(horizontal = 18.dp, vertical = 7.dp),
                                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp
                                    )
                                }
                                Spacer(Modifier.height(14.dp))
                                OutlinedButton(
                                    onClick = { navController.navigate(Routes.ScoreBreakdown.route) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("View Breakdown", fontSize = 13.sp)
                                    Spacer(Modifier.width(4.dp))
                                    Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp))
                                }
                            }
                        } else {
                            Column(
                                Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("No Score Yet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Text(
                                    "Upload your M-Pesa SMS to get your HustleScore",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Button(
                                    onClick = { navController.navigate(Routes.UploadSms.route) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Upload, null, tint = GreenBrand, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Upload SMS", color = GreenBrand, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── STAT CARDS ────────────────────────────────────────────────────────
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).offset(y = (-16).dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashStatCard(Modifier.weight(1f), "Income",   "KSh ${(totalIncome/1000).toInt()}k",   Icons.Default.TrendingUp,   GreenBrand, GreenPale)
                DashStatCard(Modifier.weight(1f), "Expenses", "KSh ${(totalExpenses/1000).toInt()}k", Icons.Default.TrendingDown, Rust,       RustPale)
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).offset(y = (-8).dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashStatCard(Modifier.weight(1f), "Savings",      "KSh ${(totalSavings/1000).toInt()}k", Icons.Default.Savings,    Teal, TealPale)
                DashStatCard(Modifier.weight(1f), "Transactions", "${transactions.size}",                 Icons.Default.SwapHoriz,  Plum, PlumPale)
            }
        }

        // ── QUICK ACTIONS ─────────────────────────────────────────────────────
        item {
            Text(
                "Quick Actions",
                fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextMain,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        item {
            DashQuickActionCard(
                title = "Download Report",
                subtitle = "Get your credit profile PDF",
                icon = Icons.Default.Description,
                iconColor = Teal,
                iconBg = TealPale
            ) { navController.navigate(Routes.CreditReport.route) }
        }

        item { Spacer(Modifier.height(4.dp)) }

        item {
            DashQuickActionCard(
                title = "Financial Advice",
                subtitle = "Personalized tips for you",
                icon = Icons.Default.Lightbulb,
                iconColor = Amber,
                iconBg = AmberPale
            ) { navController.navigate(Routes.FinancialAdvice.route) }
        }

        // ── RECENT TRANSACTIONS ───────────────────────────────────────────────
        if (transactions.isNotEmpty()) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    Arrangement.SpaceBetween, Alignment.CenterVertically
                ) {
                    Text("Recent Transactions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextMain)
                    Text(
                        "View all →", fontSize = 12.sp, color = GreenBrand, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { navController.navigate(Routes.ScoreBreakdown.route) }
                    )
                }
            }
            item {
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    RoundedCornerShape(20.dp),
                    CardDefaults.cardColors(CreamCard),
                    CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(vertical = 6.dp)) {
                        transactions.take(5).forEachIndexed { index, tx ->
                            DashTransactionRow(tx)
                            if (index < minOf(transactions.size, 5) - 1)
                                Divider(Modifier.padding(horizontal = 16.dp), color = CreamBorder, thickness = 0.8.dp)
                        }
                    }
                }
            }
        }

        // ── ADD TRANSACTION BUTTON ────────────────────────────────────────────
        item {
            Button(
                onClick = { navController.navigate(Routes.AddTransaction.route) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp).height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenBrand),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Transaction", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// ─── Stat Card ────────────────────────────────────────────────────────────────
@Composable
fun DashStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    bgColor: Color
) {
    Card(modifier, RoundedCornerShape(18.dp), CardDefaults.cardColors(CreamCard), CardDefaults.cardElevation(3.dp)) {
        Column(Modifier.padding(14.dp)) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(bgColor),
                Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(label, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

// ─── Quick Action Card ────────────────────────────────────────────────────────
@Composable
fun DashQuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    onClick: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onClick() },
        RoundedCornerShape(18.dp),
        CardDefaults.cardColors(CreamCard),
        CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(iconBg),
                Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextMain)
                Text(subtitle, color = TextMuted, fontSize = 12.sp)
            }
            Box(
                Modifier.size(30.dp).clip(CircleShape).background(GreenSurface),
                Alignment.Center
            ) {
                Icon(Icons.Default.ChevronRight, null, tint = GreenBrand, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ─── Transaction Row ──────────────────────────────────────────────────────────
@Composable
fun DashTransactionRow(transaction: Transaction) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amtColor = if (isIncome) GreenBrand else Rust
    val bgColor  = if (isIncome) GreenPale  else RustPale

    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(38.dp).clip(CircleShape).background(bgColor), Alignment.Center) {
            Text(if (isIncome) "↑" else "↓", color = amtColor, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                transaction.description.orEmpty().ifBlank { "M-Pesa Transaction" },
                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextMain,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(transaction.date, fontSize = 11.sp, color = TextMuted)
        }
        Text(
            "${if (isIncome) "+" else "-"}KSh ${String.format("%,.0f", transaction.amount)}",
            color = amtColor, fontWeight = FontWeight.Bold, fontSize = 13.sp
        )
    }
}

// Keep old names as aliases so existing call sites compile
@Composable
fun StatCard(modifier: Modifier = Modifier, label: String, value: String, color: Color, bgColor: Color) =
    DashStatCard(modifier, label, value, Icons.Default.AttachMoney, color, bgColor)

@Composable
fun QuickActionCard(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) =
    DashQuickActionCard(title, subtitle, icon, color, color.copy(alpha = 0.12f), onClick)

@Composable
fun TransactionListItem(transaction: Transaction) = DashTransactionRow(transaction)

// ─── Preview ──────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardScreenPreview() {
    HustleScoreTheme { DashboardScreen(navController = rememberNavController()) }
}
package com.serah.hustlescore.ui.screens.user

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.serah.hustlescore.data.algorithm.HustleScoreEngine
import com.serah.hustlescore.models.HustleScore
import com.serah.hustlescore.models.Transaction
import com.serah.hustlescore.models.TransactionType
import com.serah.hustlescore.navigation.Routes
import com.serah.hustlescore.ui.theme.ThemeViewModel

// ─── Fixed colour tokens (always the same regardless of theme) ────────────────
private val GreenDeep   = Color(0xFF062110)
private val GreenMid    = Color(0xFF0F4523)
private val GreenBrand  = Color(0xFF1A7A3C)
private val GreenLight  = Color(0xFF25A355)
private val GreenAccent = Color(0xFF4FCB78)
private val GreenPale   = Color(0xFFD6F0E0)
private val Amber       = Color(0xFFD4860A)
private val AmberPale   = Color(0xFFFFF0D4)
private val Rust        = Color(0xFFC0521A)
private val RustPale    = Color(0xFFFFECE0)
private val Teal        = Color(0xFF1A7A6E)
private val TealPale    = Color(0xFFD4F0EE)
private val Plum        = Color(0xFF6E3A7A)
private val PlumPale    = Color(0xFFF0E4F5)

// ─── Data helper ──────────────────────────────────────────────────────────────
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

// ─── Screen ───────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()

    // ── Theme-aware colours ───────────────────────────────────────────────────
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFFAF8F4)
    val cardColor       = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFFFDF9)
    val primaryText     = if (isDarkMode) Color.White       else Color(0xFF0C200F)
    val secondaryText   = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5C7A63)
    val dividerColor    = if (isDarkMode) Color(0xFF374151) else Color(0xFFE8E0D4)
    val surfaceGreen    = if (isDarkMode) Color(0xFF1A2E1F) else Color(0xFFEEF8F2)

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
                    isAdmin = s.getValue(String::class.java) == "admin"
                }
                override fun onCancelled(e: DatabaseError) {}
            })
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

    if (loading) {
        Box(Modifier.fillMaxSize().background(backgroundColor), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CircularProgressIndicator(color = GreenBrand, strokeWidth = 3.dp, modifier = Modifier.size(44.dp))
                Text("Loading your data…", color = secondaryText, fontSize = 13.sp)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(backgroundColor),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {

        // ── HERO HEADER ──────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(GreenDeep, GreenMid, GreenBrand)))
            ) {
                Box(Modifier.size(200.dp).offset(x = (-40).dp, y = (-60).dp)
                    .background(Color.White.copy(alpha = 0.04f), CircleShape))
                Box(Modifier.size(140.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = 20.dp)
                    .background(GreenAccent.copy(alpha = 0.08f), CircleShape))

                Column(Modifier.padding(horizontal = 22.dp, vertical = 28.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(46.dp).shadow(8.dp, CircleShape)
                                    .background(Brush.linearGradient(listOf(GreenAccent, GreenLight)), CircleShape),
                                Alignment.Center
                            ) {
                                Text(initials, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                            }
                            Spacer(Modifier.width(13.dp))
                            Column {
                                Text(greeting, color = Color.White.copy(alpha = 0.65f), fontSize = 12.sp)
                                Text(firstName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (isAdmin) {
                                Box(
                                    Modifier.clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .clickable { navController.navigate(Routes.AdminDashboard.route) }
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.AdminPanelSettings, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Text("Admin", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            IconButton(onClick = { navController.navigate(Routes.UserDashboard.route) }) {
                                Icon(Icons.Default.Dashboard, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(24.dp))
                            }
                            Box {
                                IconButton(onClick = { navController.navigate(Routes.Notifications.route) }) {
                                    Icon(Icons.Default.Notifications, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(24.dp))
                                }
                                Badge(Modifier.align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp), containerColor = Color(0xFFF59E0B)) {
                                    Text("3", fontSize = 9.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    if (score != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(shape = RoundedCornerShape(20.dp), color = GreenAccent.copy(alpha = 0.22f)) {
                                Text("HUSTLE SCORE", Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
                            }
                            Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.18f)) {
                                Text(score.grade.label, Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) {
                            Column {
                                Text("${score.totalScore}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 64.sp, lineHeight = 68.sp)
                                Text("out of 1000", color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${transactions.size}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                                Text("transactions", color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.18f))) {
                            Box(Modifier.fillMaxWidth(score.totalScore / 1000f).height(8.dp).clip(RoundedCornerShape(4.dp))
                                .background(Brush.linearGradient(listOf(GreenAccent, Color.White))))
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Poor", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                            Text("Excellent", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                        }
                    } else {
                        Spacer(Modifier.height(4.dp))
                        Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.10f)) {
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.18f)), Alignment.Center) {
                                    Icon(Icons.Default.Upload, null, tint = Color.White, modifier = Modifier.size(26.dp))
                                }
                                Column(Modifier.weight(1f)) {
                                    Text("No Score Yet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text("Upload your M-Pesa SMS to calculate your HustleScore",
                                        color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, lineHeight = 18.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Button(onClick = { navController.navigate(Routes.UploadSms.route) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().height(50.dp)) {
                            Icon(Icons.Default.Upload, null, tint = GreenBrand, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Upload M-Pesa SMS", color = GreenBrand, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        // ── STAT CARDS ───────────────────────────────────────────────────────
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp).offset(y = (-16).dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeStatCard(Modifier.weight(1f), "Income",   "KSh ${(totalIncome/1000).toInt()}k",   Icons.Default.TrendingUp,   GreenBrand, GreenPale,  cardColor, secondaryText)
                HomeStatCard(Modifier.weight(1f), "Expenses", "KSh ${(totalExpenses/1000).toInt()}k", Icons.Default.TrendingDown, Rust,       RustPale,   cardColor, secondaryText)
                HomeStatCard(Modifier.weight(1f), "Txns",     "${transactions.size}",                 Icons.Default.SwapHoriz,   Teal,       TealPale,   cardColor, secondaryText)
            }
        }

        // ── UPDATE SCORE BANNER ───────────────────────────────────────────────
        if (score != null) {
            item {
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)
                    .clickable { navController.navigate(Routes.UploadSms.route) },
                    RoundedCornerShape(16.dp), CardDefaults.cardColors(surfaceGreen), CardDefaults.cardElevation(0.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(GreenBrand), Alignment.Center) {
                            Icon(Icons.Default.Refresh, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Update Your Score", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = primaryText)
                            Text("Upload new M-Pesa SMS to refresh", fontSize = 11.sp, color = secondaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Icon(Icons.Default.ArrowForwardIos, null, tint = GreenBrand, modifier = Modifier.size(16.dp))
                    }
                }
            }
            item { Spacer(Modifier.height(6.dp)) }
        }

        // ── SCORE BREAKDOWN CARD ──────────────────────────────────────────────
        if (score != null) {
            item {
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    RoundedCornerShape(22.dp), CardDefaults.cardColors(cardColor), CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("Score Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = primaryText)
                            Text("View all →", fontSize = 12.sp, color = GreenBrand, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { navController.navigate(Routes.ScoreBreakdown.route) })
                        }
                        Spacer(Modifier.height(16.dp))
                        listOf(
                            Triple("Income",   score.incomeScore,   GreenLight),
                            Triple("Savings",  score.savingsScore,  Teal),
                            Triple("Expenses", score.expenseScore,  Amber),
                            Triple("Activity", score.activityScore, Plum),
                            Triple("Debt",     score.debtScore,     Rust)
                        ).forEach { (label, value, color) ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(8.dp).clip(CircleShape).background(color))
                                Spacer(Modifier.width(8.dp))
                                Text(label, Modifier.width(64.dp), fontSize = 12.sp, color = secondaryText, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.width(8.dp))
                                Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(color.copy(alpha = 0.12f))) {
                                    Box(Modifier.fillMaxWidth(value / 1000f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(color))
                                }
                                Spacer(Modifier.width(12.dp))
                                Text("$value", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.width(32.dp))
                            }
                        }
                    }
                }
            }
        }

        // ── QUICK ACTIONS ─────────────────────────────────────────────────────
        item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = primaryText)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureTile(Modifier.weight(1f), "My Score",     "Full breakdown", Icons.Default.TrendingUp,  GreenBrand) { navController.navigate(Routes.ScoreBreakdown.route) }
                    FeatureTile(Modifier.weight(1f), "Credit Report","Download PDF",  Icons.Default.Description, Teal)       { navController.navigate(Routes.CreditReport.route) }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureTile(Modifier.weight(1f), "Get Advice", "Financial tips",   Icons.Default.Lightbulb, Amber)   { navController.navigate(Routes.FinancialAdvice.route) }
                    FeatureTile(Modifier.weight(1f), "Profile",    "Account settings", Icons.Default.Person,    GreenMid) { navController.navigate(Routes.UserProfile.route) }
                }
                if (isAdmin) {
                    Spacer(Modifier.height(12.dp))
                    FeatureTile(Modifier.fillMaxWidth(), "Admin Panel", "Manage platform & users", Icons.Default.AdminPanelSettings, Rust) { navController.navigate(Routes.AdminDashboard.route) }
                }
            }
        }

        // ── RECENT TRANSACTIONS ───────────────────────────────────────────────
        if (transactions.isNotEmpty()) {
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Recent Transactions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = primaryText)
                    Text("View all →", fontSize = 12.sp, color = GreenBrand, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { navController.navigate(Routes.ScoreBreakdown.route) })
                }
            }
            item {
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    RoundedCornerShape(22.dp), CardDefaults.cardColors(cardColor), CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(vertical = 6.dp)) {
                        transactions.take(5).forEachIndexed { index, tx ->
                            val isIncome = tx.type == TransactionType.INCOME
                            val amtColor = if (isIncome) GreenBrand else Rust
                            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(40.dp).clip(CircleShape).background(if (isIncome) GreenPale else RustPale), Alignment.Center) {
                                    Text(if (isIncome) "↑" else "↓", color = amtColor, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(tx.description.orEmpty().ifBlank { "M-Pesa Transaction" },
                                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = primaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(tx.date, fontSize = 11.sp, color = secondaryText)
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("${if (isIncome) "+" else "-"}KSh ${String.format("%,.0f", tx.amount)}",
                                    color = amtColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            if (index < minOf(transactions.size, 5) - 1)
                                Divider(Modifier.padding(horizontal = 16.dp), color = dividerColor, thickness = 0.8.dp)
                        }
                    }
                }
            }
        }
    }
}

// ─── Stat Card ────────────────────────────────────────────────────────────────
@Composable
private fun HomeStatCard(
    modifier: Modifier = Modifier,
    label: String, value: String,
    icon: ImageVector, valueColor: Color, iconBg: Color,
    cardColor: Color, secondaryText: Color
) {
    Card(modifier, RoundedCornerShape(18.dp), CardDefaults.cardColors(cardColor), CardDefaults.cardElevation(3.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.Start) {
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(iconBg), Alignment.Center) {
                Icon(icon, null, tint = valueColor, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = valueColor)
            Text(label, fontSize = 10.sp, color = secondaryText, fontWeight = FontWeight.Medium)
        }
    }
}

// ─── Feature Tile (unchanged — always on gradient, no dark tweak needed) ──────
@Composable
private fun FeatureTile(modifier: Modifier = Modifier, title: String, subtitle: String, icon: ImageVector, bgColor: Color, onClick: () -> Unit) {
    Box(modifier.height(100.dp).clip(RoundedCornerShape(20.dp))
        .background(Brush.linearGradient(listOf(bgColor, bgColor.copy(alpha = 0.75f)))).clickable { onClick() }) {
        Box(Modifier.size(80.dp).align(Alignment.BottomEnd).offset(x = 20.dp, y = 20.dp).background(Color.White.copy(alpha = 0.08f), CircleShape))
        Column(Modifier.fillMaxSize().padding(14.dp), Arrangement.SpaceBetween) {
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.22f)), Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.72f), fontSize = 10.sp)
            }
        }
    }
}
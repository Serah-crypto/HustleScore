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

// ─── Colour tokens ───────────────────────────────────────────────────────────
// Greens
private val GreenDeep    = Color(0xFF062110)   // darkest forest
private val GreenMid     = Color(0xFF0F4523)   // header mid
private val GreenBrand   = Color(0xFF1A7A3C)   // primary brand
private val GreenLight   = Color(0xFF25A355)   // lighter action
private val GreenAccent  = Color(0xFF4FCB78)   // highlights / progress
private val GreenPale    = Color(0xFFD6F0E0)   // very light tint for chips/tags
private val GreenSurface = Color(0xFFEEF8F2)   // income card bg, breakdown track bg

// Warm neutrals (replaces all grey)
private val Cream        = Color(0xFFFAF8F4)   // page background
private val CreamCard    = Color(0xFFFFFDF9)   // card background
private val CreamBorder  = Color(0xFFE8E0D4)   // dividers
private val Amber        = Color(0xFFD4860A)   // expenses accent (warm, no blue)
private val AmberPale    = Color(0xFFFFF0D4)   // expense card bg
private val Rust         = Color(0xFFC0521A)   // debt / negative accent (warm red-orange)
private val RustPale     = Color(0xFFFFECE0)   // debt pale bg
private val Teal         = Color(0xFF1A7A6E)   // savings (green-teal, no blue)
private val TealPale     = Color(0xFFD4F0EE)   // savings pale bg
private val Plum         = Color(0xFF6E3A7A)   // activity (warm purple, not blue-purple)
private val PlumPale     = Color(0xFFF0E4F5)   // activity pale bg

// Text
private val TextMain     = Color(0xFF0C200F)   // near-black green tint
private val TextMuted    = Color(0xFF5C7A63)   // muted green-warm

// ─── Data helper (unchanged) ──────────────────────────────────────────────────
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

    // ── Loading ──────────────────────────────────────────────────────────────
    if (loading) {
        Box(Modifier.fillMaxSize().background(Cream), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CircularProgressIndicator(color = GreenBrand, strokeWidth = 3.dp, modifier = Modifier.size(44.dp))
                Text("Loading your data…", color = TextMuted, fontSize = 13.sp)
            }
        }
        return
    }

    // ── Main content ──────────────────────────────────────────────────────────
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Cream),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {

        // ── HERO HEADER ──────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(GreenDeep, GreenMid, GreenBrand)
                        )
                    )
            ) {
                // Decorative circle blobs
                Box(
                    Modifier
                        .size(200.dp)
                        .offset(x = (-40).dp, y = (-60).dp)
                        .background(Color.White.copy(alpha = 0.04f), CircleShape)
                )
                Box(
                    Modifier
                        .size(140.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 40.dp, y = 20.dp)
                        .background(GreenAccent.copy(alpha = 0.08f), CircleShape)
                )

                Column(Modifier.padding(horizontal = 22.dp, vertical = 28.dp)) {

                    // Top bar
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Avatar
                            Box(
                                Modifier
                                    .size(46.dp)
                                    .shadow(8.dp, CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(GreenAccent, GreenLight)),
                                        CircleShape
                                    ),
                                Alignment.Center
                            ) {
                                Text(initials, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                            }
                            Spacer(Modifier.width(13.dp))
                            Column {
                                Text(greeting, color = Color.White.copy(alpha = 0.65f), fontSize = 12.sp, fontWeight = FontWeight.Normal)
                                Text(firstName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (isAdmin) {
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(10.dp))
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
                                Badge(
                                    Modifier.align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp),
                                    containerColor = Color(0xFFF59E0B)
                                ) {
                                    Text("3", fontSize = 9.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Score section
                    if (score != null) {
                        // Score pill / grade chip
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = GreenAccent.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    "HUSTLE SCORE",
                                    Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    color = GreenAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp
                                )
                            }
                            Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.18f)) {
                                Text(
                                    score.grade.label,
                                    Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) {
                            Column {
                                Text(
                                    "${score.totalScore}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 64.sp,
                                    lineHeight = 68.sp
                                )
                                Text("out of 1000", color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${transactions.size}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                                Text("transactions", color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Progress bar
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(score.totalScore / 1000f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.linearGradient(listOf(GreenAccent, Color.White))
                                    )
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Poor", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                            Text("Excellent", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                        }

                    } else {
                        // Empty state
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.10f)
                        ) {
                            Row(
                                Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.18f)),
                                    Alignment.Center
                                ) {
                                    Icon(Icons.Default.Upload, null, tint = Color.White, modifier = Modifier.size(26.dp))
                                }
                                Column(Modifier.weight(1f)) {
                                    Text("No Score Yet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text(
                                        "Upload your M-Pesa SMS to calculate your HustleScore",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = { navController.navigate(Routes.UploadSms.route) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-16).dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EnhancedStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Income",
                    value = "KSh ${(totalIncome / 1000).toInt()}k",
                    icon = Icons.Default.TrendingUp,
                    valueColor = GreenBrand,
                    iconBg = GreenPale
                )
                EnhancedStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Expenses",
                    value = "KSh ${(totalExpenses / 1000).toInt()}k",
                    icon = Icons.Default.TrendingDown,
                    valueColor = Rust,
                    iconBg = RustPale
                )
                EnhancedStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Txns",
                    value = "${transactions.size}",
                    icon = Icons.Default.SwapHoriz,
                    valueColor = Teal,
                    iconBg = TealPale
                )
            }
        }

        // ── UPDATE SCORE BANNER (only when score exists) ──────────────────
        if (score != null) {
            item {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .clickable { navController.navigate(Routes.UploadSms.route) },
                    RoundedCornerShape(16.dp),
                    CardDefaults.cardColors(GreenSurface),
                    CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GreenBrand),
                            Alignment.Center
                        ) {
                            Icon(Icons.Default.Refresh, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Update Your Score", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextMain)
                            Text(
                                "Upload new M-Pesa SMS to refresh",
                                fontSize = 11.sp, color = TextMuted,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(Icons.Default.ArrowForwardIos, null, tint = GreenBrand, modifier = Modifier.size(16.dp))
                    }
                }
            }
            item { Spacer(Modifier.height(6.dp)) }
        }

        // ── SCORE BREAKDOWN CARD ──────────────────────────────────────────
        if (score != null) {
            item {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    RoundedCornerShape(22.dp),
                    CardDefaults.cardColors(CreamCard),
                    CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("Score Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextMain)
                            Text(
                                "View all →",
                                fontSize = 12.sp, color = GreenBrand, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { navController.navigate(Routes.ScoreBreakdown.route) }
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        listOf(
                            Triple("Income",   score.incomeScore,   GreenLight),
                            Triple("Savings",  score.savingsScore,  Teal),
                            Triple("Expenses", score.expenseScore,  Amber),
                            Triple("Activity", score.activityScore, Plum),
                            Triple("Debt",     score.debtScore,     Rust)
                        ).forEach { (label, value, color) ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Dot indicator
                                Box(
                                    Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(label, Modifier.width(64.dp), fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(color.copy(alpha = 0.12f))
                                ) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth(value / 1000f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(color)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "$value",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color,
                                    modifier = Modifier.width(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── QUICK ACTIONS ─────────────────────────────────────────────────
        item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextMain)
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureTile(
                        Modifier.weight(1f), "My Score", "Full breakdown",
                        Icons.Default.TrendingUp, GreenBrand
                    ) { navController.navigate(Routes.ScoreBreakdown.route) }
                    FeatureTile(
                        Modifier.weight(1f), "Credit Report", "Download PDF",
                        Icons.Default.Description, Teal
                    ) { navController.navigate(Routes.CreditReport.route) }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureTile(
                        Modifier.weight(1f), "Get Advice", "Financial tips",
                        Icons.Default.Lightbulb, Amber
                    ) { navController.navigate(Routes.FinancialAdvice.route) }
                    FeatureTile(
                        Modifier.weight(1f), "Profile", "Account settings",
                        Icons.Default.Person, GreenMid
                    ) { navController.navigate(Routes.UserProfile.route) }
                }
                if (isAdmin) {
                    Spacer(Modifier.height(12.dp))
                    FeatureTile(
                        Modifier.fillMaxWidth(), "Admin Panel", "Manage platform & users",
                        Icons.Default.AdminPanelSettings, Rust
                    ) { navController.navigate(Routes.AdminDashboard.route) }
                }
            }
        }

        // ── RECENT TRANSACTIONS ───────────────────────────────────────────
        if (transactions.isNotEmpty()) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    Arrangement.SpaceBetween,
                    Alignment.CenterVertically
                ) {
                    Text("Recent Transactions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextMain)
                    Text(
                        "View all →",
                        fontSize = 12.sp, color = GreenBrand, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { navController.navigate(Routes.ScoreBreakdown.route) }
                    )
                }
            }
            item {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    RoundedCornerShape(22.dp),
                    CardDefaults.cardColors(CreamCard),
                    CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(vertical = 6.dp)) {
                        transactions.take(5).forEachIndexed { index, tx ->
                            val isIncome = tx.type == TransactionType.INCOME
                            val amtColor = if (isIncome) GreenBrand else Rust

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Icon circle
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isIncome) GreenPale else RustPale),
                                    Alignment.Center
                                ) {
                                    Text(
                                        if (isIncome) "↑" else "↓",
                                        color = amtColor,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        tx.description.orEmpty().ifBlank { "M-Pesa Transaction" },
                                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                        color = TextMain, maxLines = 1, overflow = TextOverflow.Ellipsis
                                    )
                                    Text(tx.date, fontSize = 11.sp, color = TextMuted)
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "${if (isIncome) "+" else "-"}KSh ${String.format("%,.0f", tx.amount)}",
                                    color = amtColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            if (index < minOf(transactions.size, 5) - 1) {
                                Divider(
                                    Modifier.padding(horizontal = 16.dp),
                                    color = CreamBorder,
                                    thickness = 0.8.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Stat Card ────────────────────────────────────────────────────────────────
@Composable
private fun EnhancedStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    valueColor: Color,
    iconBg: Color
) {
    Card(
        modifier,
        RoundedCornerShape(18.dp),
        CardDefaults.cardColors(CreamCard),
        CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                Alignment.Center
            ) {
                Icon(icon, null, tint = valueColor, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = valueColor)
            Text(label, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Medium)
        }
    }
}

// ─── Feature Tile ─────────────────────────────────────────────────────────────
@Composable
private fun FeatureTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    bgColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier
            .height(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(bgColor, bgColor.copy(alpha = 0.75f))
                )
            )
            .clickable { onClick() }
    ) {
        // Decorative background circle
        Box(
            Modifier
                .size(80.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 20.dp, y = 20.dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(14.dp),
            Arrangement.SpaceBetween
        ) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.22f)),
                Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.72f), fontSize = 10.sp)
            }
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HustleScoreTheme { HomeScreen(navController = rememberNavController()) }
}
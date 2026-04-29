package com.serah.hustlescore.ui.screens.user



import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.serah.hustlescore.data.algorithm.HustleScoreEngine
import com.serah.hustlescore.models.HustleScore
import com.serah.hustlescore.models.Transaction
import com.serah.hustlescore.models.TransactionType
import com.serah.hustlescore.navigation.Screen
import com.serah.hustlescore.ui.theme.BackgroundGray
import com.serah.hustlescore.ui.theme.HustleGreen
import com.serah.hustlescore.ui.theme.TextPrimary
import com.serah.hustlescore.ui.theme.TextSecondary

@Composable
fun HomeScreen(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firstName = currentUser?.displayName?.split(" ")?.firstOrNull() ?: "there"
    val initials = currentUser?.displayName
        ?.split(" ")
        ?.mapNotNull { it.firstOrNull()?.toString() }
        ?.take(2)
        ?.joinToString("") ?: "U"

    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var scoreData by remember { mutableStateOf<HustleScore?>(null) }
    var loading by remember { mutableStateOf(true) }

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
                override fun onCancelled(error: DatabaseError) {
                    loading = false
                }
            })
    }

    val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val score = scoreData

    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {

        // ── Top Header Banner ──────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF0F4C2A), HustleGreen)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 28.dp)
            ) {
                Column {
                    // Top Row: Avatar + Notifications
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = greeting,
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = firstName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        // Notifications Bell
                        Box {
                            IconButton(
                                onClick = { navController.navigate(Screen.Notifications.route) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Badge(
                                modifier = Modifier.align(Alignment.TopEnd),
                                containerColor = Color(0xFFF59E0B)
                            ) {
                                Text(text = "3", fontSize = 9.sp, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Score Hero Section
                    if (score != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "Your HustleScore",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${score.totalScore}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 52.sp,
                                    lineHeight = 56.sp
                                )
                                Text(
                                    text = "out of 1000",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = score.grade.label,
                                        modifier = Modifier.padding(
                                            horizontal = 14.dp,
                                            vertical = 6.dp
                                        ),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${transactions.size} transactions",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Score Progress Bar
                        Column {
                            LinearProgressIndicator(
                                progress = { score.totalScore / 1000f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.25f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Poor",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "Excellent",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 10.sp
                                )
                            }
                        }

                    } else {
                        // No score yet CTA
                        Column {
                            Text(
                                text = "No Score Yet",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                            Text(
                                text = "Upload your M-Pesa SMS to get your HustleScore",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { navController.navigate(Screen.Upload.route) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Upload,
                                    contentDescription = null,
                                    tint = HustleGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Upload M-Pesa SMS",
                                    color = HustleGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Quick Stats ────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Income",
                    value = "KSh ${(totalIncome / 1000).toInt()}k",
                    valueColor = Color(0xFF16A34A),
                    bgColor = Color(0xFFDCFCE7)
                )
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Expenses",
                    value = "KSh ${(totalExpenses / 1000).toInt()}k",
                    valueColor = Color(0xFFDC2626),
                    bgColor = Color(0xFFFEE2E2)
                )
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Transactions",
                    value = "${transactions.size}",
                    valueColor = Color(0xFF7C3AED),
                    bgColor = Color(0xFFEDE9FE)
                )
            }
        }

        // ── Upload CTA (if has score) ──────────────────────────────────
        if (score != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { navController.navigate(Screen.Upload.route) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(HustleGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Update Your Score",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Upload new M-Pesa SMS to refresh your score",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = HustleGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(4.dp)) }
        }

        // ── Score Factor Summary ───────────────────────────────────────
        if (score != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Score Breakdown",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "View all →",
                                fontSize = 12.sp,
                                color = HustleGreen,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable {
                                    navController.navigate(Screen.ScoreBreakdown.route)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val factors = listOf(
                            Triple("Income", score.incomeScore, Color(0xFF16A34A)),
                            Triple("Savings", score.savingsScore, Color(0xFF2563EB)),
                            Triple("Expenses", score.expenseScore, Color(0xFFF59E0B)),
                            Triple("Activity", score.activityScore, Color(0xFF7C3AED)),
                            Triple("Debt", score.debtScore, Color(0xFFEA580C))
                        )

                        factors.forEach { (label, value, color) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.width(70.dp),
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFE5E7EB))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(value / 1000f)
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(color)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "$value",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color,
                                    modifier = Modifier.width(36.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Feature Cards ──────────────────────────────────────────────
        item {
            Text(
                text = "What would you like to do?",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureTile(
                        modifier = Modifier.weight(1f),
                        title = "My Score",
                        subtitle = "Full breakdown",
                        icon = Icons.Default.TrendingUp,
                        bgColor = Color(0xFF1D4ED8),
                        onClick = { navController.navigate(Screen.ScoreBreakdown.route) }
                    )
                    FeatureTile(
                        modifier = Modifier.weight(1f),
                        title = "Credit Report",
                        subtitle = "Download PDF",
                        icon = Icons.Default.Description,
                        bgColor = Color(0xFF7C3AED),
                        onClick = { navController.navigate(Screen.CreditReport.route) }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureTile(
                        modifier = Modifier.weight(1f),
                        title = "Get Advice",
                        subtitle = "Financial tips",
                        icon = Icons.Default.Lightbulb,
                        bgColor = Color(0xFFD97706),
                        onClick = { navController.navigate(Screen.Advice.route) }
                    )
                    FeatureTile(
                        modifier = Modifier.weight(1f),
                        title = "Profile",
                        subtitle = "Account settings",
                        icon = Icons.Default.Person,
                        bgColor = Color(0xFF0F766E),
                        onClick = { navController.navigate(Screen.Profile.route) }
                    )
                }
            }
        }

        // ── Recent Transactions ────────────────────────────────────────
        if (transactions.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "View all →",
                        fontSize = 12.sp,
                        color = HustleGreen,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.ScoreBreakdown.route)
                        }
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        transactions.take(5).forEachIndexed { index, tx ->
                            val isIncome = tx.type == TransactionType.INCOME
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
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
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tx.description.ifBlank { "M-Pesa Transaction" },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = tx.date,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    text = "${if (isIncome) "+" else "-"}KSh ${
                                        String.format("%,.0f", tx.amount)
                                    }",
                                    color = if (isIncome) Color(0xFF16A34A) else Color(0xFFDC2626),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                            if (index < minOf(transactions.size, 5) - 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .padding(horizontal = 16.dp)
                                        .background(Color(0xFFE5E7EB))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Supporting Composables ─────────────────────────────────────────────────

@Composable
private fun MiniStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color,
    bgColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = valueColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun FeatureTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    bgColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(26.dp)
            )
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
            }
        }
    }
}
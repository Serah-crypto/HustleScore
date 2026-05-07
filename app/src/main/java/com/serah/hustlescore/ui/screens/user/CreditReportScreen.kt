package com.serah.hustlescore.ui.screens.user

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.serah.hustlescore.ui.theme.BackgroundGray
import com.serah.hustlescore.ui.theme.HustleGreen
import com.serah.hustlescore.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun CreditReportScreen(navController: NavController) {
    val context = LocalContext.current
    var scoreData by remember { mutableStateOf<HustleScore?>(null) }
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var generating by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val scope = rememberCoroutineScope()           // FIX 1: moved here, single scope at top level

    LaunchedEffect(Unit) {
        val uid = currentUser?.uid ?: return@LaunchedEffect
        FirebaseDatabase.getInstance().getReference("transactions/$uid")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val txs = snapshot.children.mapNotNull { it.getValue(Transaction::class.java) }
                    transactions = txs
                    scoreData = if (txs.isNotEmpty()) HustleScoreEngine.calculate(txs) else null
                    loading = false
                }
                override fun onCancelled(error: DatabaseError) { loading = false }
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
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Credit Report", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Your official HustleScore profile", fontSize = 13.sp, color = TextSecondary)
            }
            // FIX 2: single Button, coroutine-based, correct text
            if (scoreData != null) {
                Button(
                    onClick = {
                        scope.launch {
                            generating = true
                            withContext(Dispatchers.IO) {
                                generatePDF(
                                    context, scoreData!!, transactions,
                                    currentUser?.displayName ?: "User",
                                    currentUser?.email ?: ""
                                )
                            }
                            generating = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HustleGreen),
                    enabled = !generating
                ) {
                    if (generating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (generating) "Generating..." else "Download PDF",
                        fontSize = 13.sp
                    )
                }
            }
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HustleGreen)
            }
        } else if (scoreData == null) {
            Card(shape = RoundedCornerShape(16.dp), border = BorderStroke(2.dp, Color(0xFFE5E7EB))) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(48.dp), tint = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    Text("No Report Available", fontWeight = FontWeight.SemiBold)
                    Text("Upload your M-Pesa SMS to generate a report.", color = TextSecondary, fontSize = 13.sp)
                }
            }
        } else {
            val score = scoreData!!
            val totalIncome   = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val totalSavings  = transactions.filter { it.type == TransactionType.SAVINGS }.sumOf { it.amount }



            // Report Header Card
            Card(shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(8.dp)) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(HustleGreen, Color(0xFF145A32))))
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text("HustleScore", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Text("Alternative Credit Profile", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                Text(
                                    java.text.SimpleDateFormat("dd MMMM yyyy").format(java.util.Date()),
                                    color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${score.totalScore}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 40.sp)
                                Text("/ 1000", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.2f)) {
                                    Text(
                                        score.grade.label,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color = Color.White, fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            InfoField(Modifier.weight(1f), "Full Name", currentUser?.displayName ?: "N/A")
                            InfoField(Modifier.weight(1f), "Report Date", java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date()))
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            InfoField(Modifier.weight(1f), "Email", currentUser?.email ?: "N/A")
                            InfoField(Modifier.weight(1f), "Transactions", "${transactions.size}")
                        }
                    }
                }
            }

            // Financial Summary
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Financial Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        SummaryItem("Income",   "KSh ${(totalIncome   / 1000).toInt()}k", Color(0xFF16A34A))
                        SummaryItem("Expenses", "KSh ${(totalExpenses / 1000).toInt()}k", Color(0xFFDC2626))
                        SummaryItem("Savings",  "KSh ${(totalSavings  / 1000).toInt()}k", Color(0xFF2563EB))
                    }
                }
            }

            // Score Breakdown
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Score Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(12.dp))
                    listOf(
                        "Income Stability"     to score.incomeScore,
                        "Savings Ratio"        to score.savingsScore,
                        "Expense Control"      to score.expenseScore,
                        "Transaction Activity" to score.activityScore,
                        "Debt Behavior"        to score.debtScore
                    ).forEach { (label, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, modifier = Modifier.width(130.dp), fontSize = 12.sp, color = TextSecondary)
                            Box(
                                modifier = Modifier.weight(1f).height(6.dp)
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
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "$value", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(36.dp),
                                color = HustleScoreEngine.getScoreColor(score.totalScore)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoField(modifier: Modifier = Modifier, label: String, value: String) {
    Column(modifier = modifier.padding(end = 8.dp)) {
        Text(label, fontSize = 10.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 12.sp, color = TextSecondary)
    }
}

fun generatePDF(
    context: Context,
    score: HustleScore,
    transactions: List<Transaction>,
    name: String,
    email: String
) {
    val document = PdfDocument()
    val W = 595f
    val paint = Paint().apply { isAntiAlias = true }
    val dateStr = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())

    // ── PAGE 1 ──────────────────────────────────────────────────────────────
    val page1 = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
    val c = page1.canvas

    // Green header band
    paint.color = android.graphics.Color.parseColor("#1E8449")
    c.drawRect(0f, 0f, W, 90f, paint)

    paint.color = android.graphics.Color.WHITE
    paint.textSize = 26f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    c.drawText("HustleScore", 30f, 50f, paint)

    paint.textSize = 11f
    paint.typeface = Typeface.DEFAULT
    paint.color = android.graphics.Color.parseColor("#A9DFBF")
    c.drawText("Alternative Credit Profile  •  $dateStr", 30f, 72f, paint)

    // Big score badge (top-right)
    paint.color = android.graphics.Color.parseColor("#145A32")
    c.drawRoundRect(RectF(W - 120f, 10f, W - 10f, 80f), 12f, 12f, paint)
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 30f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val scoreText = "${score.totalScore}"
    val scoreW = paint.measureText(scoreText)
    c.drawText(scoreText, W - 65f - scoreW / 2, 52f, paint)
    paint.textSize = 10f
    paint.typeface = Typeface.DEFAULT
    paint.color = android.graphics.Color.parseColor("#A9DFBF")
    c.drawText("/ 1000  ${score.grade.label.uppercase()}", W - 110f, 70f, paint)

    // Personal info box
    paint.color = android.graphics.Color.parseColor("#F0F4F8")
    c.drawRoundRect(RectF(20f, 102f, W - 20f, 185f), 10f, 10f, paint)
    paint.color = android.graphics.Color.parseColor("#6B7280")
    paint.textSize = 10f
    c.drawText("FULL NAME", 32f, 120f, paint)
    c.drawText("EMAIL", 32f, 148f, paint)
    c.drawText("TRANSACTIONS", 310f, 120f, paint)
    c.drawText("REPORT DATE", 310f, 148f, paint)
    paint.color = android.graphics.Color.BLACK
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    c.drawText(name.ifBlank { "N/A" }, 32f, 135f, paint)
    c.drawText(email.ifBlank { "N/A" }, 32f, 163f, paint)
    c.drawText("${transactions.size}", 310f, 135f, paint)
    c.drawText(dateStr, 310f, 163f, paint)
    paint.typeface = Typeface.DEFAULT

    // ── Financial Summary ────────────────────────────────────────────────────
    var y = 205f
    sectionTitle(c, paint, "Financial Summary", y)
    y += 22f

    val totalIncome   = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val totalSavings  = transactions.filter { it.type == TransactionType.SAVINGS }.sumOf { it.amount }

    val summaries = listOf(
        Triple("Total Income",   "KSh ${"%,.0f".format(totalIncome)}",   "#16A34A"),
        Triple("Total Expenses", "KSh ${"%,.0f".format(totalExpenses)}", "#DC2626"),
        Triple("Total Savings",  "KSh ${"%,.0f".format(totalSavings)}",  "#2563EB")
    )
    val boxW = (W - 60f) / 3f
    summaries.forEachIndexed { i, (label, value, hex) ->
        val x = 20f + i * (boxW + 10f)
        paint.color = android.graphics.Color.parseColor(hex).let {
            android.graphics.Color.argb(30, android.graphics.Color.red(it), android.graphics.Color.green(it), android.graphics.Color.blue(it))
        }
        c.drawRoundRect(RectF(x, y, x + boxW, y + 52f), 8f, 8f, paint)
        paint.color = android.graphics.Color.parseColor(hex)
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        c.drawText(value, x + 8f, y + 24f, paint)
        paint.color = android.graphics.Color.parseColor("#6B7280")
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        c.drawText(label, x + 8f, y + 42f, paint)
    }
    y += 70f

    // ── Score Breakdown ───────────────────────────────────────────────────────
    sectionTitle(c, paint, "Score Breakdown", y)
    y += 22f

    val factors = listOf(
        Triple("Income Stability",     score.incomeScore,   "#16A34A"),
        Triple("Savings Ratio",        score.savingsScore,  "#2563EB"),
        Triple("Expense Control",      score.expenseScore,  "#D97706"),
        Triple("Transaction Activity", score.activityScore, "#7C3AED"),
        Triple("Debt Behaviour",       score.debtScore,     "#DB2777")
    )
    factors.forEach { (label, value, hex) ->
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        c.drawText(label, 30f, y + 4f, paint)

        // Bar track
        paint.color = android.graphics.Color.parseColor("#E5E7EB")
        c.drawRoundRect(RectF(175f, y - 8f, 475f, y + 6f), 4f, 4f, paint)

        // Bar fill
        val fillW = (value / 1000f) * 300f
        paint.color = android.graphics.Color.parseColor(hex)
        if (fillW > 0) c.drawRoundRect(RectF(175f, y - 8f, 175f + fillW, y + 6f), 4f, 4f, paint)

        // Value label
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        c.drawText("$value", 482f, y + 4f, paint)

        y += 28f
    }
    y += 10f

    // ── Financial Advice ──────────────────────────────────────────────────────
    val advice = HustleScoreEngine.getAdvice(score)
    sectionTitle(c, paint, "Financial Advice", y)
    y += 22f

    advice.take(4).forEach { tip ->
        paint.color = android.graphics.Color.parseColor("#F0FDF4")
        c.drawRoundRect(RectF(20f, y - 12f, W - 20f, y + 22f), 8f, 8f, paint)
        paint.color = android.graphics.Color.parseColor("#166534")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        c.drawText("${tip.icon}  ${tip.title}", 30f, y + 2f, paint)
        paint.color = android.graphics.Color.parseColor("#374151")
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        c.drawText(tip.description.take(90), 30f, y + 16f, paint)
        y += 44f
    }

    // Footer
    paint.color = android.graphics.Color.parseColor("#9CA3AF")
    paint.textSize = 9f
    c.drawText("Generated by HustleScore  •  Confidential  •  Page 1", 30f, 825f, paint)

    document.finishPage(page1)

    // ── PAGE 2 — Transaction History ─────────────────────────────────────────
    val page2 = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 2).create())
    val c2 = page2.canvas

    paint.color = android.graphics.Color.parseColor("#1E8449")
    c2.drawRect(0f, 0f, W, 55f, paint)
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 18f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    c2.drawText("Transaction History", 30f, 36f, paint)

    // Table header
    var y2 = 75f
    paint.color = android.graphics.Color.parseColor("#F3F4F6")
    c2.drawRect(20f, y2, W - 20f, y2 + 22f, paint)
    paint.color = android.graphics.Color.parseColor("#374151")
    paint.textSize = 10f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    c2.drawText("Date",        30f,  y2 + 15f, paint)
    c2.drawText("Description", 120f, y2 + 15f, paint)
    c2.drawText("Type",        330f, y2 + 15f, paint)
    c2.drawText("Amount (KSh)",430f, y2 + 15f, paint)
    y2 += 28f

    val typeColors = mapOf(
        TransactionType.INCOME         to "#16A34A",
        TransactionType.EXPENSE        to "#DC2626",
        TransactionType.SAVINGS        to "#2563EB",
        TransactionType.LOAN_REPAYMENT to "#D97706"
    )

    transactions.take(25).forEachIndexed { idx, tx ->
        if (idx % 2 == 0) {
            paint.color = android.graphics.Color.parseColor("#F9FAFB")
            c2.drawRect(20f, y2 - 10f, W - 20f, y2 + 10f, paint)
        }
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        c2.drawText(tx.date.take(10), 30f, y2 + 4f, paint)
        c2.drawText(tx.description.take(30), 120f, y2 + 4f, paint)

        paint.color = android.graphics.Color.parseColor(typeColors[tx.type] ?: "#374151")
        c2.drawText(tx.type.name, 330f, y2 + 4f, paint)

        paint.color = android.graphics.Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        c2.drawText("%,.0f".format(tx.amount), 430f, y2 + 4f, paint)

        y2 += 22f
        if (y2 > 800f) return@forEachIndexed   // stop if page runs out
    }

    paint.color = android.graphics.Color.parseColor("#9CA3AF")
    paint.textSize = 9f
    paint.typeface = Typeface.DEFAULT
    c2.drawText("Generated by HustleScore  •  Confidential  •  Page 2", 30f, 825f, paint)

    document.finishPage(page2)

    // ── Save & Open ───────────────────────────────────────────────────────────
    try {
        val file = File(context.cacheDir, "HustleScore_Report.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Helper — draws a green section title with an underline
private fun sectionTitle(canvas: Canvas, paint: Paint, title: String, y: Float) {
    paint.color = android.graphics.Color.parseColor("#1E8449")
    paint.textSize = 13f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(title, 20f, y, paint)
    paint.strokeWidth = 1.5f
    canvas.drawLine(20f, y + 4f, 575f, y + 4f, paint)
    paint.strokeWidth = 0f
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreditReportScreenPreview() {
    HustleScoreTheme {   // Replace with your actual Theme name if different
        CreditReportScreen(navController = rememberNavController())
    }
}
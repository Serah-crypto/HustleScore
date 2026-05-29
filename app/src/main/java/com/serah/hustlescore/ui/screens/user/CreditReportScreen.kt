package com.serah.hustlescore.ui.screens.user

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.serah.hustlescore.data.algorithm.HustleScoreEngine
import com.serah.hustlescore.models.HustleScore
import com.serah.hustlescore.models.Transaction
import com.serah.hustlescore.models.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

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
fun CreditReportScreen(navController: NavController) {
    val context      = LocalContext.current
    var scoreData    by remember { mutableStateOf<HustleScore?>(null) }
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var loading      by remember { mutableStateOf(true) }
    var generating   by remember { mutableStateOf(false) }
    var errorMsg     by remember { mutableStateOf<String?>(null) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val scope       = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val uid = currentUser?.uid ?: run { loading = false; return@LaunchedEffect }
        FirebaseDatabase.getInstance().getReference("transactions/$uid")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val txs = snapshot.children.mapNotNull { child ->
                        try {
                            @Suppress("UNCHECKED_CAST")
                            val map = child.value as? Map<String, Any?> ?: return@mapNotNull null
                            val typeStr = (map["typeRaw"] as? String) ?: (map["type"] as? String) ?: "INCOME"
                            Transaction(
                                type        = runCatching { TransactionType.valueOf(typeStr) }.getOrDefault(TransactionType.INCOME),
                                UserId      = map["UserId"] as? String,
                                id          = (map["id"] as? String) ?: child.key,
                                amount      = (map["amount"] as? Number)?.toDouble() ?: 0.0,
                                date        = map["date"] as? String ?: "",
                                description = map["description"] as? String,
                                category    = map["category"] as? String,
                                mpesaRef    = map["mpesaRef"] as? String,
                                rawSms      = map["rawSms"] as? String,
                                balance     = (map["balance"] as? Number)?.toDouble(),
                                timestamp   = (map["timestamp"] as? Number)?.toLong() ?: 0L
                            )
                        } catch (e: Exception) { null }
                    }
                    transactions = txs
                    scoreData    = if (txs.isNotEmpty()) HustleScoreEngine.calculate(txs) else null
                    loading      = false
                }
                override fun onCancelled(error: DatabaseError) { loading = false }
            })
    }

    // ✅ Fixed: using INCOME, EXPENSE instead of RECEIVED, SENT
    val totalIncome   = transactions.filter { it.type == TransactionType.INCOME  }.sumOf { it.amount }
    val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val totalSavings  = transactions.filter { it.type == TransactionType.SAVINGS }.sumOf { it.amount }
    val netBalance    = totalIncome - totalExpenses

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
    ) {

        // ── HEADER ────────────────────────────────────────────────────────────
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(GreenDeep, GreenMid, GreenBrand)))
        ) {
            Box(Modifier.size(140.dp).align(Alignment.TopEnd).offset(x = 30.dp, y = (-20).dp)
                .background(GreenAccent.copy(alpha = 0.07f), CircleShape))
            Box(Modifier.size(80.dp).align(Alignment.BottomStart).offset(x = (-20).dp, y = 20.dp)
                .background(Color.White.copy(alpha = 0.04f), CircleShape))

            Column(Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .clickable { navController.popBackStack() },
                            Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowBackIosNew, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("Credit Report", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Your official HustleScore profile", fontSize = 12.sp, color = Color.White.copy(alpha = 0.65f))
                        }
                    }

                    if (scoreData != null) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable(enabled = !generating) {
                                    scope.launch {
                                        generating = true
                                        errorMsg   = null
                                        try {
                                            withContext(Dispatchers.IO) {
                                                generatePDF(
                                                    context      = context,
                                                    score        = scoreData!!,
                                                    transactions = transactions,
                                                    name         = currentUser?.displayName.orEmpty(),
                                                    email        = currentUser?.email.orEmpty()
                                                )
                                            }
                                        } catch (e: Exception) { errorMsg = "PDF failed: ${e.message}" }
                                        generating = false
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (generating) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Download, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    if (generating) "Generating…" else "Download PDF",
                                    color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                if (scoreData != null) {
                    val score = scoreData!!
                    Spacer(Modifier.height(20.dp))
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.10f)) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("HustleScore", color = Color.White.copy(alpha = 0.65f), fontSize = 12.sp)
                                Text(
                                    "${score.totalScore}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 52.sp,
                                    lineHeight = 56.sp
                                )
                                Text("out of 1000", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(shape = RoundedCornerShape(12.dp), color = GreenAccent.copy(alpha = 0.22f)) {
                                    Text(
                                        score.grade.label,
                                        Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        color = GreenAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp
                                    )
                                }
                                Text("${transactions.size} transactions", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.18f))) {
                        Box(
                            Modifier.fillMaxWidth(score.totalScore / 1000f).height(7.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.linearGradient(listOf(GreenAccent, Color.White)))
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Poor", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                        Text("Excellent", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                    }
                }
            }
        }

        // ── BODY ──────────────────────────────────────────────────────────────
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

            errorMsg?.let {
                Card(
                    colors = CardDefaults.cardColors(RustPale),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Rust.copy(alpha = 0.3f))
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = Rust, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(it, color = Rust, fontSize = 13.sp)
                    }
                }
            }

            when {
                loading -> {
                    Box(Modifier.fillMaxWidth().padding(48.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            CircularProgressIndicator(color = GreenBrand, strokeWidth = 3.dp, modifier = Modifier.size(44.dp))
                            Text("Loading your report…", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }

                scoreData == null -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(CreamCard),
                        border = BorderStroke(1.5.dp, CreamBorder)
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                Modifier.size(64.dp).clip(CircleShape).background(GreenPale),
                                Alignment.Center
                            ) {
                                Icon(Icons.Default.Description, null, modifier = Modifier.size(32.dp), tint = GreenBrand)
                            }
                            Text("No Report Available", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextMain)
                            Text(
                                "Upload your M-Pesa SMS to generate a report.",
                                color = TextMuted, fontSize = 13.sp
                            )
                        }
                    }
                }

                else -> {
                    val score = scoreData!!

                    // ── Financial Summary ─────────────────────────────────────
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(CreamCard),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(Modifier.size(6.dp).clip(CircleShape).background(GreenBrand))
                                Text("Financial Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextMain)
                            }
                            Spacer(Modifier.height(14.dp))

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                MiniFinCard(Modifier.weight(1f), "Income",   "KSh ${formatAmount(totalIncome)}",   GreenBrand, GreenPale)
                                MiniFinCard(Modifier.weight(1f), "Expenses", "KSh ${formatAmount(totalExpenses)}", Rust,       RustPale)
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                MiniFinCard(Modifier.weight(1f), "Savings",  "KSh ${formatAmount(totalSavings)}",  Teal,       TealPale)
                                MiniFinCard(
                                    Modifier.weight(1f),
                                    "Net Balance",
                                    "KSh ${formatAmount(netBalance)}",
                                    if (netBalance >= 0) GreenBrand else Rust,
                                    if (netBalance >= 0) GreenPale  else RustPale
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Divider(color = CreamBorder, thickness = 0.8.dp)
                            Spacer(Modifier.height(10.dp))
                            ReportSummaryRow("Total Transactions", "${transactions.size}", TextMain)
                        }
                    }

                    // ── Score Breakdown ───────────────────────────────────────
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(CreamCard),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(Modifier.size(6.dp).clip(CircleShape).background(GreenBrand))
                                Text("Score Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextMain)
                            }
                            Spacer(Modifier.height(16.dp))

                            listOf(
                                Triple("Income Score",   score.incomeScore,   GreenLight),
                                Triple("Savings Score",  score.savingsScore,  Teal),
                                Triple("Expense Score",  score.expenseScore,  Amber),
                                Triple("Activity Score", score.activityScore, Plum),
                                Triple("Debt Score",     score.debtScore,     Rust)
                            ).forEach { (label, value, color) ->
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(Modifier.size(8.dp).clip(CircleShape).background(color))
                                    Spacer(Modifier.width(8.dp))
                                    Text(label, Modifier.width(100.dp), fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                                    Box(
                                        Modifier.weight(1f).height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(color.copy(alpha = 0.12f))
                                    ) {
                                        Box(
                                            Modifier.fillMaxWidth(value / 1000f).height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(color)
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        "$value",
                                        fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                        color = color, modifier = Modifier.width(36.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ── Recent Transactions ───────────────────────────────────
                    if (transactions.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(CreamCard),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    Arrangement.SpaceBetween, Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(Modifier.size(6.dp).clip(CircleShape).background(GreenBrand))
                                        Text("Recent Transactions", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextMain)
                                    }
                                    Text("${transactions.size} total", fontSize = 11.sp, color = TextMuted)
                                }
                                Spacer(Modifier.height(12.dp))

                                transactions.take(5).forEachIndexed { index, tx ->
                                    // ✅ Fixed: using INCOME instead of RECEIVED
                                    val isIncome = tx.type == TransactionType.INCOME
                                    val amtColor = if (isIncome) GreenBrand else Rust
                                    val bgColor  = if (isIncome) GreenPale  else RustPale

                                    Row(
                                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(Modifier.size(36.dp).clip(CircleShape).background(bgColor), Alignment.Center) {
                                            Text(if (isIncome) "↑" else "↓", color = amtColor, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                tx.description.orEmpty().ifBlank { "M-Pesa Transaction" },
                                                fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextMain,
                                                maxLines = 1
                                            )
                                            Text(tx.date, fontSize = 10.sp, color = TextMuted)
                                        }
                                        Text(
                                            "${if (isIncome) "+" else "-"}KSh ${formatAmount(tx.amount)}",
                                            color = amtColor, fontWeight = FontWeight.Bold, fontSize = 12.sp
                                        )
                                    }
                                    if (index < minOf(transactions.size, 5) - 1)
                                        Divider(color = CreamBorder, thickness = 0.8.dp)
                                }
                            }
                        }
                    }

                    // ── Download CTA ──────────────────────────────────────────
                    Button(
                        onClick = {
                            scope.launch {
                                generating = true; errorMsg = null
                                try {
                                    withContext(Dispatchers.IO) {
                                        generatePDF(context, scoreData!!, transactions,
                                            currentUser?.displayName.orEmpty(), currentUser?.email.orEmpty())
                                    }
                                } catch (e: Exception) { errorMsg = "PDF failed: ${e.message}" }
                                generating = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        enabled = !generating,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenBrand),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (generating) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Generating PDF…", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        } else {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Download Full PDF Report", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ─── Mini Finance Card ────────────────────────────────────────────────────────
@Composable
private fun MiniFinCard(modifier: Modifier, label: String, value: String, color: Color, bg: Color) {
    Box(modifier.clip(RoundedCornerShape(14.dp)).background(bg).padding(12.dp)) {
        Column {
            Text(label, fontSize = 10.sp, color = color.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

// ─── Summary Row ─────────────────────────────────────────────────────────────
@Composable
private fun ReportSummaryRow(label: String, value: String, valueColor: Color = TextMain) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, color = TextMuted, fontSize = 13.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = valueColor)
    }
}

private fun formatAmount(amount: Double): String =
    NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 2 }.format(amount)

// ─── PDF Generation ───────────────────────────────────────────────────────────
fun generatePDF(
    context: Context,
    score: HustleScore,
    transactions: List<Transaction>,
    name: String,
    email: String
) {
    // ✅ Fixed: using INCOME, EXPENSE instead of RECEIVED, SENT
    val totalIncome   = transactions.filter { it.type == TransactionType.INCOME  }.sumOf { it.amount }
    val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val totalSavings  = transactions.filter { it.type == TransactionType.SAVINGS }.sumOf { it.amount }
    val today         = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

    val document   = PdfDocument()
    val pageWidth  = 595
    val pageHeight = 842

    val brandGreen  = AndroidColor.rgb(26, 122, 60)
    val deepGreen   = AndroidColor.rgb(6,  33,  16)
    val palGreen    = AndroidColor.rgb(214,240,224)
    val surfGreen   = AndroidColor.rgb(238,248,242)
    val amber       = AndroidColor.rgb(212,134, 10)
    val rust        = AndroidColor.rgb(192, 82, 26)
    val rustPale    = AndroidColor.rgb(255,236,224)
    val teal        = AndroidColor.rgb(26, 122,110)
    val tealPale    = AndroidColor.rgb(212,240,238)
    val cream       = AndroidColor.rgb(250,248,244)

    fun paint(
        color: Int         = AndroidColor.BLACK,
        size:  Float       = 12f,
        bold:  Boolean     = false,
        align: Paint.Align = Paint.Align.LEFT
    ) = Paint().apply {
        this.color       = color
        this.textSize    = size
        this.typeface    = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        this.textAlign   = align
        this.isAntiAlias = true
    }

    fun rectPaint(color: Int, style: Paint.Style = Paint.Style.FILL) = Paint().apply {
        this.color       = color
        this.style       = style
        this.isAntiAlias = true
    }

    val page1 = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
    val c1: Canvas = page1.canvas

    c1.drawRect(RectF(0f, 0f, pageWidth.toFloat(), 90f), rectPaint(deepGreen))
    c1.drawRect(RectF(0f, 55f, pageWidth.toFloat(), 90f), rectPaint(brandGreen))
    c1.drawText("HustleScore", 30f, 38f, paint(AndroidColor.WHITE, 26f, bold = true))
    c1.drawText("Official Credit Report", 30f, 60f, paint(AndroidColor.WHITE, 13f))
    c1.drawText("Generated: $today", pageWidth - 30f, 60f, paint(AndroidColor.WHITE, 11f, align = Paint.Align.RIGHT))
    c1.drawRect(RectF(0f, 90f, pageWidth.toFloat(), 92f), rectPaint(palGreen))

    var y = 120f
    c1.drawText("Report For", 30f, y, paint(AndroidColor.GRAY, 10f))
    y += 18f
    c1.drawText(name.ifBlank { "N/A" }, 30f, y, paint(AndroidColor.BLACK, 15f, bold = true))
    y += 16f
    c1.drawText(email.ifBlank { "N/A" }, 30f, y, paint(AndroidColor.GRAY, 11f))

    val bl = pageWidth - 160f; val bt = 105f; val br = pageWidth - 30f; val bb = 180f
    c1.drawRoundRect(RectF(bl, bt, br, bb), 12f, 12f, rectPaint(surfGreen))
    c1.drawRoundRect(RectF(bl, bt, br, bb), 12f, 12f,
        rectPaint(brandGreen, Paint.Style.STROKE).apply { strokeWidth = 1.5f })
    c1.drawText("${score.totalScore}", (bl + br) / 2f, bt + 44f,
        paint(brandGreen, 36f, bold = true, align = Paint.Align.CENTER))
    c1.drawText("Grade: ${score.grade}", (bl + br) / 2f, bt + 62f,
        paint(AndroidColor.GRAY, 11f, align = Paint.Align.CENTER))

    y = 200f
    c1.drawRect(RectF(30f, y, pageWidth - 30f, y + 1f), rectPaint(AndroidColor.LTGRAY))
    y += 20f
    c1.drawText("FINANCIAL SUMMARY", 30f, y, paint(brandGreen, 10f, bold = true))
    y += 20f

    fun summaryRow(label: String, value: String, yPos: Float, rowBg: Int = cream): Float {
        val rowBottom = yPos + 32f
        c1.drawRect(RectF(30f, yPos, pageWidth - 30f, rowBottom), rectPaint(rowBg))
        c1.drawText(label, 44f, yPos + 21f, paint(AndroidColor.DKGRAY, 12f))
        c1.drawText(value, pageWidth - 44f, yPos + 21f,
            paint(AndroidColor.BLACK, 12f, bold = true, align = Paint.Align.RIGHT))
        return rowBottom + 4f
    }

    y = summaryRow("Total Income",       "KES ${formatAmount(totalIncome)}",   y)
    y = summaryRow("Total Expenses",     "KES ${formatAmount(totalExpenses)}", y, rustPale)
    y = summaryRow("Total Savings",      "KES ${formatAmount(totalSavings)}",  y, tealPale)
    y = summaryRow("Total Transactions", "${transactions.size}",               y)
    y = summaryRow("Net Balance",        "KES ${formatAmount(totalIncome - totalExpenses)}", y,
        if (totalIncome >= totalExpenses) palGreen else rustPale)

    y += 16f
    c1.drawRect(RectF(30f, y, pageWidth - 30f, y + 1f), rectPaint(AndroidColor.LTGRAY))
    y += 20f
    c1.drawText("SCORE BREAKDOWN", 30f, y, paint(brandGreen, 10f, bold = true))
    y += 20f

    listOf(
        Triple("Income Score",   score.incomeScore,   palGreen),
        Triple("Savings Score",  score.savingsScore,  tealPale),
        Triple("Expense Score",  score.expenseScore,  cream),
        Triple("Activity Score", score.activityScore, cream),
        Triple("Debt Score",     score.debtScore,     rustPale)
    ).forEach { (label, value, bg) -> y = summaryRow(label, "$value / 1000", y, bg) }

    c1.drawRect(RectF(0f, pageHeight - 40f, pageWidth.toFloat(), pageHeight.toFloat()), rectPaint(surfGreen))
    c1.drawText("HustleScore — Empowering Kenya's Informal Workers  |  Page 1",
        pageWidth / 2f, pageHeight - 14f, paint(AndroidColor.GRAY, 9f, align = Paint.Align.CENTER))

    document.finishPage(page1)

    val page2 = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create())
    val c2: Canvas = page2.canvas

    c2.drawRect(RectF(0f, 0f, pageWidth.toFloat(), 70f), rectPaint(deepGreen))
    c2.drawRect(RectF(0f, 40f, pageWidth.toFloat(), 70f), rectPaint(brandGreen))
    c2.drawText("Transaction History", 30f, 35f, paint(AndroidColor.WHITE, 18f, bold = true))
    c2.drawText("Last ${minOf(transactions.size, 25)} transactions", 30f, 54f, paint(AndroidColor.WHITE, 11f))

    var y2 = 90f
    c2.drawRect(RectF(20f, y2, pageWidth - 20f, y2 + 26f), rectPaint(brandGreen))

    val colDate = 30f; val colDesc = 120f; val colType = 330f; val colAmount = pageWidth - 30f
    val hp = paint(AndroidColor.WHITE, 11f, bold = true)
    c2.drawText("Date",         colDate,   y2 + 18f, hp)
    c2.drawText("Description",  colDesc,   y2 + 18f, hp)
    c2.drawText("Type",         colType,   y2 + 18f, hp)
    c2.drawText("Amount (KES)", colAmount, y2 + 18f,
        paint(AndroidColor.WHITE, 11f, bold = true, align = Paint.Align.RIGHT))
    y2 += 30f

    transactions.take(25).forEachIndexed { idx, tx ->
        if (y2 > pageHeight - 60f) return@forEachIndexed
        val rowBg = if (idx % 2 == 0) AndroidColor.WHITE else cream
        c2.drawRect(RectF(20f, y2, pageWidth - 20f, y2 + 28f), rectPaint(rowBg))

        val rp = paint(AndroidColor.DKGRAY, 10f)
        c2.drawText(tx.date.take(10),                  colDate,  y2 + 19f, rp)
        c2.drawText(tx.description.orEmpty().take(30), colDesc,  y2 + 19f, rp)

        // ✅ Fixed: using INCOME, EXPENSE instead of RECEIVED, SENT
        val typeColor = when (tx.type) {
            TransactionType.INCOME          -> brandGreen
            TransactionType.EXPENSE         -> rust
            TransactionType.SAVINGS         -> teal
            TransactionType.LOAN_RECEIVED   -> amber
            TransactionType.LOAN_REPAYMENT  -> rust
            else                            -> AndroidColor.GRAY
        }
        c2.drawText(tx.type.name, colType, y2 + 19f, paint(typeColor, 10f, bold = true))
        c2.drawText(formatAmount(tx.amount), colAmount, y2 + 19f,
            paint(AndroidColor.BLACK, 10f, bold = true, align = Paint.Align.RIGHT))
        c2.drawRect(RectF(20f, y2 + 28f, pageWidth - 20f, y2 + 29f), rectPaint(AndroidColor.LTGRAY))
        y2 += 29f
    }

    c2.drawRect(RectF(0f, pageHeight - 40f, pageWidth.toFloat(), pageHeight.toFloat()), rectPaint(surfGreen))
    c2.drawText("HustleScore — Empowering Kenya's Informal Workers  |  Page 2",
        pageWidth / 2f, pageHeight - 14f, paint(AndroidColor.GRAY, 9f, align = Paint.Align.CENTER))

    document.finishPage(page2)

    val fileName = "HustleScore_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
    val file     = File(context.getExternalFilesDir(null), fileName)
    FileOutputStream(file).use { document.writeTo(it) }
    document.close()

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    context.startActivity(
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
}
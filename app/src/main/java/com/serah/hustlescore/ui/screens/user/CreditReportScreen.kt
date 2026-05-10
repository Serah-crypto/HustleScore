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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.serah.hustlescore.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun CreditReportScreen(navController: NavController) {
    val context      = LocalContext.current
    var scoreData    by remember { mutableStateOf<HustleScore?>(null) }
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var loading      by remember { mutableStateOf(true) }
    var generating   by remember { mutableStateOf(false) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val scope       = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val uid = currentUser?.uid ?: return@LaunchedEffect
        FirebaseDatabase.getInstance().getReference("transactions/$uid")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val txs = snapshot.children.mapNotNull { it.getValue(Transaction::class.java) }
                    transactions = txs
                    scoreData    = if (txs.isNotEmpty()) HustleScoreEngine.calculate(txs) else null
                    loading      = false
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

            if (scoreData != null) {
                Button(
                    onClick = {
                        scope.launch {
                            generating = true
                            withContext(Dispatchers.IO) {
                                generatePDF(
                                    context,
                                    scoreData!!,
                                    transactions,
                                    currentUser?.displayName.orEmpty(),
                                    currentUser?.email.orEmpty()
                                )
                            }
                            generating = false
                        }
                    },
                    colors  = ButtonDefaults.buttonColors(containerColor = HustleGreen),
                    enabled = !generating
                ) {
                    if (generating) {
                        CircularProgressIndicator(
                            color       = Color.White,
                            modifier    = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(if (generating) "Generating..." else "Download PDF", fontSize = 13.sp)
                }
            }
        }

        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = HustleGreen) }
            }
            scoreData == null -> {
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
            }
            else -> {
                val score        = scoreData!!
                val totalIncome  = transactions.filter { it.type == TransactionType.RECEIVED }.sumOf { it.amount }
                val totalExpenses= transactions.filter { it.type == TransactionType.SENT    }.sumOf { it.amount }
                val totalSavings = transactions.filter { it.type == TransactionType.SAVINGS  }.sumOf { it.amount }

                // Score card
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("HustleScore", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${score.totalScore}", fontSize = 48.sp, fontWeight = FontWeight.Black, color = HustleGreen)
                        Text("Grade: ${score.grade}", fontSize = 14.sp, color = TextSecondary)
                    }
                }

                // Summary card
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Financial Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        SummaryRow("Total Income",   "KES ${formatAmount(totalIncome)}")
                        SummaryRow("Total Expenses", "KES ${formatAmount(totalExpenses)}")
                        SummaryRow("Total Savings",  "KES ${formatAmount(totalSavings)}")
                        SummaryRow("Transactions",   "${transactions.size}")
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
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
    // ── Computed totals ───────────────────────────────────────────────────────
    val totalIncome   = transactions.filter { it.type == TransactionType.RECEIVED }.sumOf { it.amount }
    val totalExpenses = transactions.filter { it.type == TransactionType.SENT     }.sumOf { it.amount }
    val totalSavings  = transactions.filter { it.type == TransactionType.SAVINGS   }.sumOf { it.amount }
    val today         = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

    // ── PDF document setup ────────────────────────────────────────────────────
    val document   = PdfDocument()
    val pageWidth  = 595   // A4 points width
    val pageHeight = 842   // A4 points height

    // ── Reusable paints ───────────────────────────────────────────────────────
    fun paint(
        color:     Int   = AndroidColor.BLACK,
        size:      Float = 12f,
        bold:      Boolean = false,
        align:     Paint.Align = Paint.Align.LEFT
    ) = Paint().apply {
        this.color     = color
        this.textSize  = size
        this.typeface  = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        this.textAlign = align
        this.isAntiAlias = true
    }

    fun rectPaint(color: Int, style: Paint.Style = Paint.Style.FILL) = Paint().apply {
        this.color   = color
        this.style   = style
        this.isAntiAlias = true
    }

    // ────────────────────────────────────────────────────────────────────────
    // PAGE 1 — Summary
    // ────────────────────────────────────────────────────────────────────────
    val page1Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
    val page1     = document.startPage(page1Info)
    val c1: Canvas = page1.canvas

    // Header bar (green)
    c1.drawRect(RectF(0f, 0f, pageWidth.toFloat(), 90f), rectPaint(AndroidColor.rgb(22, 163, 74)))

    // Header text
    c1.drawText("HustleScore", 30f, 38f, paint(AndroidColor.WHITE, 26f, bold = true))
    c1.drawText("Official Credit Report", 30f, 60f, paint(AndroidColor.WHITE, 13f))
    c1.drawText("Generated: $today", pageWidth - 30f, 60f,
        paint(AndroidColor.WHITE, 11f, align = Paint.Align.RIGHT))

    // Divider
    c1.drawRect(RectF(0f, 90f, pageWidth.toFloat(), 92f), rectPaint(AndroidColor.rgb(187, 247, 208)))

    // User info section
    var y = 120f
    c1.drawText("Report For", 30f, y, paint(AndroidColor.GRAY, 10f))
    y += 18f
    c1.drawText(name.ifBlank { "N/A" }, 30f, y, paint(AndroidColor.BLACK, 15f, bold = true))
    y += 16f
    c1.drawText(email.ifBlank { "N/A" }, 30f, y, paint(AndroidColor.GRAY, 11f))

    // Score badge box
    val badgeLeft   = pageWidth - 160f
    val badgeTop    = 105f
    val badgeRight  = pageWidth - 30f
    val badgeBottom = 175f
    c1.drawRoundRect(RectF(badgeLeft, badgeTop, badgeRight, badgeBottom), 12f, 12f,
        rectPaint(AndroidColor.rgb(240, 253, 244)))
    c1.drawRoundRect(RectF(badgeLeft, badgeTop, badgeRight, badgeBottom), 12f, 12f,
        rectPaint(AndroidColor.rgb(22, 163, 74), Paint.Style.STROKE).apply { strokeWidth = 1.5f })
    c1.drawText("${score.totalScore}", (badgeLeft + badgeRight) / 2f, badgeTop + 42f,
        paint(AndroidColor.rgb(22, 163, 74), 36f, bold = true, align = Paint.Align.CENTER))
    c1.drawText("Grade: ${score.grade}", (badgeLeft + badgeRight) / 2f, badgeTop + 58f,
        paint(AndroidColor.GRAY, 11f, align = Paint.Align.CENTER))

    // Section divider
    y = 195f
    c1.drawRect(RectF(30f, y, pageWidth - 30f, y + 1f), rectPaint(AndroidColor.LTGRAY))
    y += 20f

    // Financial summary heading
    c1.drawText("FINANCIAL SUMMARY", 30f, y, paint(AndroidColor.rgb(22, 163, 74), 10f, bold = true))
    y += 20f

    // Summary rows helper
    fun summaryRow(label: String, value: String, yPos: Float): Float {
        val rowBottom = yPos + 32f
        c1.drawRect(RectF(30f, yPos, pageWidth - 30f, rowBottom),
            rectPaint(AndroidColor.rgb(248, 250, 252)))
        c1.drawText(label, 44f, yPos + 21f, paint(AndroidColor.DKGRAY, 12f))
        c1.drawText(value, pageWidth - 44f, yPos + 21f,
            paint(AndroidColor.BLACK, 12f, bold = true, align = Paint.Align.RIGHT))
        return rowBottom + 4f
    }

    y = summaryRow("Total Income",        "KES ${formatAmount(totalIncome)}",   y)
    y = summaryRow("Total Expenses",      "KES ${formatAmount(totalExpenses)}", y)
    y = summaryRow("Total Savings",       "KES ${formatAmount(totalSavings)}",  y)
    y = summaryRow("Total Transactions",  "${transactions.size}",               y)
    y = summaryRow("Net Balance",
        "KES ${formatAmount(totalIncome - totalExpenses)}", y)

    // Score breakdown heading
    y += 16f
    c1.drawRect(RectF(30f, y, pageWidth - 30f, y + 1f), rectPaint(AndroidColor.LTGRAY))
    y += 20f
    c1.drawText("SCORE BREAKDOWN", 30f, y, paint(AndroidColor.rgb(22, 163, 74), 10f, bold = true))
    y += 20f

    // Score sub-metrics (use whichever fields exist on HustleScore)
    listOf(
        "Income Regularity Score"   to score.incomeScore,
        "Savings Score"             to score.savingsScore,
        "Expense Control Score"     to score.expenseScore,
        "Transaction Volume Score"  to score.activityScore
    ).forEach { (label, value) ->
        y = summaryRow(label, "$value / 100", y)
    }

    // Footer
    c1.drawRect(RectF(0f, pageHeight - 40f, pageWidth.toFloat(), pageHeight.toFloat()),
        rectPaint(AndroidColor.rgb(240, 253, 244)))
    c1.drawText(
        "HustleScore — Empowering Kenya's Informal Workers  |  Confidential",
        pageWidth / 2f, pageHeight - 14f,
        paint(AndroidColor.GRAY, 9f, align = Paint.Align.CENTER)
    )

    document.finishPage(page1)

    // ────────────────────────────────────────────────────────────────────────
    // PAGE 2 — Transaction List
    // ────────────────────────────────────────────────────────────────────────
    val page2Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
    val page2     = document.startPage(page2Info)
    val c2: Canvas = page2.canvas

    // Header bar
    c2.drawRect(RectF(0f, 0f, pageWidth.toFloat(), 70f), rectPaint(AndroidColor.rgb(22, 163, 74)))
    c2.drawText("Transaction History", 30f, 35f, paint(AndroidColor.WHITE, 18f, bold = true))
    c2.drawText("Last ${minOf(transactions.size, 25)} transactions", 30f, 54f,
        paint(AndroidColor.WHITE, 11f))

    // Table header
    var y2 = 90f
    c2.drawRect(RectF(20f, y2, pageWidth - 20f, y2 + 26f),
        rectPaint(AndroidColor.rgb(22, 163, 74)))

    val colDate   = 30f
    val colDesc   = 120f
    val colType   = 330f
    val colAmount = pageWidth - 30f

    val headerPaint = paint(AndroidColor.WHITE, 11f, bold = true)
    c2.drawText("Date",        colDate,   y2 + 18f, headerPaint)
    c2.drawText("Description", colDesc,   y2 + 18f, headerPaint)
    c2.drawText("Type",        colType,   y2 + 18f, headerPaint)
    c2.drawText("Amount (KES)",colAmount, y2 + 18f,
        paint(AndroidColor.WHITE, 11f, bold = true, align = Paint.Align.RIGHT))

    y2 += 30f

    // Transaction rows
    transactions.take(25).forEachIndexed { idx, tx ->
        // Alternating row bg
        val rowBg = if (idx % 2 == 0) AndroidColor.WHITE else AndroidColor.rgb(248, 250, 252)
        c2.drawRect(RectF(20f, y2, pageWidth - 20f, y2 + 28f), rectPaint(rowBg))

        // Row text paint
        val rowPaint = paint(AndroidColor.DKGRAY, 10f)

        // Date
        c2.drawText(tx.date.orEmpty().take(10), colDate, y2 + 19f, rowPaint)

        // Description (truncated)
        c2.drawText(tx.description.orEmpty().take(30), colDesc, y2 + 19f, rowPaint)

        // Type badge color
        val typeColor = when (tx.type) {
            TransactionType.RECEIVED -> AndroidColor.rgb(22,  163, 74)
            TransactionType.SENT     -> AndroidColor.rgb(220, 38,  38)
            TransactionType.SAVINGS  -> AndroidColor.rgb(37,  99,  235)
            else                     -> AndroidColor.GRAY
        }
        c2.drawText(tx.type?.name ?: "—", colType, y2 + 19f,
            paint(typeColor, 10f, bold = true))

        // Amount (right-aligned)
        c2.drawText(formatAmount(tx.amount), colAmount, y2 + 19f,
            paint(AndroidColor.BLACK, 10f, bold = true, align = Paint.Align.RIGHT))

        // Row separator
        c2.drawRect(RectF(20f, y2 + 28f, pageWidth - 20f, y2 + 29f),
            rectPaint(AndroidColor.LTGRAY))

        y2 += 29f

        // Safety: start new page if we overflow (basic guard)
        if (y2 > pageHeight - 60f) return@forEachIndexed
    }

    // Footer
    c2.drawRect(RectF(0f, pageHeight - 40f, pageWidth.toFloat(), pageHeight.toFloat()),
        rectPaint(AndroidColor.rgb(240, 253, 244)))
    c2.drawText(
        "HustleScore — Empowering Kenya's Informal Workers  |  Page 2",
        pageWidth / 2f, pageHeight - 14f,
        paint(AndroidColor.GRAY, 9f, align = Paint.Align.CENTER)
    )

    document.finishPage(page2)

    // ── Save & open ───────────────────────────────────────────────────────────
    try {
        val fileName = "HustleScore_Report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
        val file     = File(context.getExternalFilesDir(null), fileName)

        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        // Open with a PDF viewer
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)

    } catch (e: Exception) {
        e.printStackTrace()
        document.close()
    }
}
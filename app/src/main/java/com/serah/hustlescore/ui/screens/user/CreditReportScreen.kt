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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
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
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    paint.color = android.graphics.Color.parseColor("#1E8449")
    canvas.drawRect(0f, 0f, 595f, 100f, paint)

    paint.color = android.graphics.Color.WHITE
    paint.textSize = 24f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("HustleScore", 30f, 45f, paint)

    paint.color = android.graphics.Color.parseColor("#F8FAFC")
    canvas.drawRect(30f, 120f, 565f, 220f, paint)

    paint.color = android.graphics.Color.BLACK
    paint.textSize = 14f
    paint.typeface = Typeface.DEFAULT
    canvas.drawText("Name: $name", 40f, 150f, paint)
    canvas.drawText("Email: $email", 40f, 170f, paint)
    canvas.drawText("Overall Score: ${score.totalScore} / 1000", 40f, 190f, paint)
    canvas.drawText("Rating: ${score.grade.label}", 40f, 210f, paint)

    var yPos = 260f
    paint.textSize = 16f
    paint.typeface = Typeface.DEFAULT_BOLD
    canvas.drawText("Score Factor Breakdown", 30f, yPos, paint)

    val factors = listOf(
        "Income Stability" to score.incomeScore,
        "Savings Ratio"    to score.savingsScore,
        "Expense Control"  to score.expenseScore,
        "Activity Score"   to score.activityScore
    )

    paint.typeface = Typeface.DEFAULT
    paint.textSize = 12f
    factors.forEach { (label, value) ->
        yPos += 30f
        paint.color = android.graphics.Color.BLACK
        canvas.drawText(label, 30f, yPos, paint)
        paint.color = android.graphics.Color.LTGRAY
        canvas.drawRect(150f, yPos - 10f, 450f, yPos, paint)
        paint.color = android.graphics.Color.parseColor("#16A34A")
        canvas.drawRect(150f, yPos - 10f, 150f + (value / 1000f * 300f), yPos, paint)
        paint.color = android.graphics.Color.BLACK
        canvas.drawText("$value", 460f, yPos, paint)
    }

    document.finishPage(page)

    try {
        val file = File(context.cacheDir, "HustleScore_Report.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
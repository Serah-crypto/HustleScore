package com.serah.hustlescore.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.test.espresso.base.Default
import com.google.firebase.database.*
import com.hustlescore.ui.theme.*
import com.serah.hustlescore.models.HustleScore
import com.serah.hustlescore.ui.screens.user.CreditReportScreen
import com.serah.hustlescore.ui.theme.BackgroundGray
import com.serah.hustlescore.ui.theme.HustleGreen
import com.serah.hustlescore.ui.theme.TextSecondary

@Composable
fun ScoringLogsScreen(navController: NavController) {
    var scores by remember { mutableStateOf<List<HustleScore>>(emptyList()) }
    var search by remember { mutableStateOf("") }
    var gradeFilter by remember { mutableStateOf("All") }
    var loading by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        FirebaseDatabase.getInstance().getReference("scores")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val all = mutableListOf<HustleScore>()
                    snapshot.children.forEach { u -> u.children.forEach { s -> s.getValue(HustleScore::class.java)?.let { all.add(it) } } }
                    scores = all
                    loading = false
                }
                override fun onCancelled(e: DatabaseError) { loading = false }
            })
    }

    val filtered = scores.filter { gradeFilter == "All" || it.grade.label == gradeFilter }
    val avgScore = if (scores.isNotEmpty()) scores.map { it.totalScore }.average().toInt() else 0

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Scoring Logs", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("${scores.size} scores · Avg: $avgScore", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = search, onValueChange = { search = it }, modifier = Modifier.weight(1f),
                    placeholder = { Text("Search...", fontSize = 13.sp) }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true)
                Box {
                    OutlinedButton(onClick = { expanded = true }) { Text(gradeFilter, fontSize = 13.sp); Icon(Icons.Default.ArrowDropDown, null) }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("All", "Excellent", "Good", "Fair", "Poor").forEach { g ->
                            DropdownMenuItem(text = { Text(g) }, onClick = { gradeFilter = g; expanded = false })
                        }
                    }
                }
            }
        }

        if (loading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = HustleGreen) }
        else LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(filtered) { _, sc ->
                val gradeColor = when (sc.grade.label) { "Excellent" -> Color(0xFF16A34A) "Good" -> Color(0xFFCA8A04) "Fair" -> Color(0xFFEA580C) else -> Color(0xFFDC2626) }
                Card(shape = RoundedCornerShape(14.dp), elevation = CardDefaults.cardElevation(3.dp)) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(HustleGreen), contentAlignment = Alignment.Center) {
                            Text("${sc.totalScore}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Score: ${sc.totalScore}/1000", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("I:${sc.incomeScore} S:${sc.savingsScore} E:${sc.expenseScore} A:${sc.activityScore}", fontSize = 11.sp, color = TextSecondary)
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = gradeColor.copy(alpha = 0.1f)) {
                            Text(sc.grade.label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = gradeColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ScoringLogsScreenPreview() {
    HustleScoreTheme {   // Replace with your actual Theme name if different
        ScoringLogsScreen(navController = rememberNavController())
    }
}
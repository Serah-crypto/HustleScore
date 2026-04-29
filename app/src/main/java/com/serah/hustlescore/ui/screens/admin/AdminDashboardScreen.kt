package com.serah.hustlescore.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.*
import com.serah.hustlescore.models.HustleScore
import com.serah.hustlescore.ui.theme.BackgroundGray
import com.serah.hustlescore.ui.theme.HustleGreen
import com.serah.hustlescore.ui.theme.TextSecondary
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.serah.hustlescore.navigation.Screen
import com.serah.hustlescore.ui.theme.TextPrimary

@Composable
fun AdminDashboardScreen(navController: NavController) {
    var userCount by remember { mutableStateOf(0) }
    var scores by remember { mutableStateOf<List<HustleScore>>(emptyList()) }
    var txCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        FirebaseDatabase.getInstance().getReference("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) { userCount = s.childrenCount.toInt() }
            override fun onCancelled(e: DatabaseError) {}
        })
        FirebaseDatabase.getInstance().getReference("scores").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                val all = mutableListOf<HustleScore>()
                s.children.forEach { userNode -> userNode.children.forEach { scoreNode -> scoreNode.getValue(HustleScore::class.java)?.let { all.add(it) } } }
                scores = all
            }
            override fun onCancelled(e: DatabaseError) {}
        })
        FirebaseDatabase.getInstance().getReference("transactions").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) { var count = 0; s.children.forEach { count += it.childrenCount.toInt() }; txCount = count }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    val avgScore = if (scores.isNotEmpty()) scores.map { it.totalScore }.average().toInt() else 0

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Admin Dashboard", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Platform overview and analytics", fontSize = 13.sp, color = TextSecondary)
            }
            Surface(shape = RoundedCornerShape(10.dp), color = HustleGreen.copy(alpha = 0.1f)) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(HustleGreen))
                    Spacer(Modifier.width(6.dp))
                    Text("Live", fontSize = 12.sp, color = HustleGreen, fontWeight = FontWeight.Medium)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AdminStatCard(Modifier.weight(1f), "Total Users", "$userCount", Icons.Default.People, Color(0xFF2563EB), Color(0xFFDBEAFE))
            AdminStatCard(Modifier.weight(1f), "Avg Score", "$avgScore", Icons.Default.TrendingUp, HustleGreen, Color(0xFFDCFCE7))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AdminStatCard(Modifier.weight(1f), "Transactions", "$txCount", Icons.Default.Receipt, Color(0xFF7C3AED), Color(0xFFEDE9FE))
            AdminStatCard(Modifier.weight(1f), "Reports", "${scores.size}", Icons.Default.Assessment, Color(0xFFF59E0B), Color(0xFFFEF3C7))
        }

        Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        listOf(
            Triple(Screen.AdminUsers.route, "Manage Users", "View and manage all registered users"),
            Triple(Screen.AdminLogs.route, "Scoring Logs", "Review all generated HustleScores"),
            Triple(Screen.AdminWeights.route, "Algorithm Weights", "Adjust scoring formula weights")
        ).forEach { (route, title, subtitle) ->
            Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate(route) }, shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(subtitle, color = TextSecondary, fontSize = 12.sp)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                }
            }
        }

        if (scores.isNotEmpty()) {
            Text("Recent Scores", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    scores.take(5).forEach { sc ->
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(HustleGreen), contentAlignment = Alignment.Center) {
                                Text("${sc.totalScore}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(sc.grade.label, modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            val gradeColor = when (sc.grade.label) { "Excellent" -> Color(0xFF16A34A) "Good" -> Color(0xFFCA8A04) "Fair" -> Color(0xFFEA580C) else -> Color(0xFFDC2626) }
                            Surface(shape = RoundedCornerShape(8.dp), color = gradeColor.copy(alpha = 0.1f)) {
                                Text(sc.grade.label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = gradeColor, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color, bgColor: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(bgColor), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AdminDashboardScreenPreview(){
    AdminDashboardScreen(rememberNavController())



}
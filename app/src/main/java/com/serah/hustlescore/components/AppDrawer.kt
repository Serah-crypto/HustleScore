package com.serah.hustlescore.components


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.serah.hustlescore.navigation.Routes
import com.serah.hustlescore.ui.theme.HustleGreen
import com.serah.hustlescore.ui.theme.HustleDark

// rest of the file stays exactly the same...

data class NavItem(val label: String, val icon: ImageVector, val route: String)

val userNavItems = listOf(
    NavItem("Dashboard", Icons.Default.Home, Routes.UserDashboard.route),
    NavItem("Upload SMS", Icons.Default.Upload, Routes.UploadSms.route),
    NavItem("My Score", Icons.Default.TrendingUp, Routes.ScoreBreakdown.route),
    NavItem("Credit Report", Icons.Default.Description, Routes.CreditReport.route),
    NavItem("Financial Advice", Icons.Default.Lightbulb, Routes.FinancialAdvice.route),
    NavItem("Notifications", Icons.Default.Notifications, Routes.Notifications.route),
    NavItem("Profile", Icons.Default.Person, Routes.Profile.route),
)

val adminNavItems = listOf(
    NavItem("Admin Dashboard", Icons.Default.Dashboard, Routes.AdminDashboard.route),
    NavItem("Users", Icons.Default.People, Routes.UsersList.route),
    NavItem("Scoring Logs", Icons.Default.Assessment, Routes.ScoringLogs.route),
    NavItem("Algorithm Weights", Icons.Default.Tune, Routes.AlgorithmWeight.route),
)

@Composable
fun AppDrawerContent(
    navController: NavController,
    currentRoute: String?,
    userName: String,
    userEmail: String,
    isAdmin: Boolean,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(HustleDark)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(HustleGreen, Color(0xFF145A32))))
                .padding(24.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("H", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("HustleScore", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Credit Intelligence", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(userName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(userEmail, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val items = if (isAdmin && currentRoute?.startsWith("admin") == true) adminNavItems else userNavItems

        items.forEach { item ->
            val isActive = currentRoute == item.route
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isActive) HustleGreen else Color.Transparent)
                    .clickable {
                        navController.navigate(item.route) { launchSingleTop = true }
                        onClose()
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(item.icon, contentDescription = null, tint = if (isActive) Color.White else Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Text(item.label, color = if (isActive) Color.White else Color.White.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Sign Out
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Red.copy(alpha = 0.15f))
                .clickable { com.google.firebase.auth.FirebaseAuth.getInstance().signOut() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Sign Out", color = Color.Red.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
        }
    }
}

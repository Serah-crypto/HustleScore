package com.serah.hustlescore.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.serah.hustlescore.navigation.Routes
import com.serah.hustlescore.ui.theme.ThemeViewModel
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode

@Composable
fun UserProfileScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()

    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF4F6F9)
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val primaryText = if (isDarkMode) Color.White else Color(0xFF111827)
    val secondaryText = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val dividerColor = if (isDarkMode) Color(0xFF374151) else Color(0xFFF3F4F6)

    var fullName   by remember { mutableStateOf(currentUser?.displayName ?: "—") }
    var email      by remember { mutableStateOf(currentUser?.email ?: "—") }
    var phone      by remember { mutableStateOf("—") }
    var occupation by remember { mutableStateOf("—") }
    var county     by remember { mutableStateOf("—") }
    var employer   by remember { mutableStateOf("—") }
    var income     by remember { mutableStateOf("—") }
    var bio        by remember { mutableStateOf("—") }
    var profileComplete by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid ?: return@LaunchedEffect
        FirebaseDatabase.getInstance()
            .getReference("Users/$uid/profile")  // ✅ Fixed: capital U
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val map = snapshot.value as? Map<String, Any?> ?: return
                    fullName   = map["fullName"]      as? String ?: fullName
                    email      = map["email"]         as? String ?: email
                    phone      = map["phone"]         as? String ?: "—"
                    occupation = map["occupation"]    as? String ?: "—"
                    county     = map["county"]        as? String ?: "—"
                    employer   = map["employer"]      as? String ?: "—"
                    income     = map["monthlyIncome"] as? String ?: "—"
                    bio        = map["bio"]           as? String ?: "—"
                    profileComplete = map["profileComplete"] as? Boolean ?: false
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    val initials = fullName.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2).joinToString("")
        .ifEmpty { "U" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF0F4C2A), Color(0xFF1E8449))))
                .padding(vertical = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                Text(fullName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(email,    color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                if (!profileComplete) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.9f)
                    ) {
                        Text(
                            "⚠ Profile Incomplete",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(Routes.UserDetailForm.route) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E8449)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (profileComplete) "Edit Profile" else "Complete Profile", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))

        val cardMod = Modifier.fillMaxWidth().padding(horizontal = 16.dp)

        Card(
            modifier = cardMod,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Personal Details", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    color = Color(0xFF1E8449), modifier = Modifier.padding(bottom = 8.dp))
                ProfileRow("Phone", phone, Icons.Default.Phone, primaryText, secondaryText, dividerColor)
                ProfileRow("County", county, Icons.Default.LocationOn, primaryText, secondaryText, dividerColor)
                ProfileRow("ID Number", "••••••••", Icons.Default.CreditCard, primaryText, secondaryText, dividerColor)
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = cardMod,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Financial Details", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    color = Color(0xFF1E8449), modifier = Modifier.padding(bottom = 8.dp))
                ProfileRow("Occupation", occupation, Icons.Default.Work, primaryText, secondaryText, dividerColor)
                ProfileRow("Employer", employer, Icons.Default.Business, primaryText, secondaryText, dividerColor)
                ProfileRow("Monthly Income", "KES $income", Icons.Default.AttachMoney, primaryText, secondaryText, dividerColor)
            }
        }

        Spacer(Modifier.height(12.dp))

        if (bio.isNotBlank() && bio != "—") {
            Card(
                modifier = cardMod,
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                        color = Color(0xFF1E8449), modifier = Modifier.padding(bottom = 8.dp))
                    Text(bio, fontSize = 14.sp, color = primaryText, lineHeight = 20.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        Card(
            modifier = cardMod,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, null, tint = Color(0xFF1E8449), modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(if (isDarkMode) "Dark Mode" else "Light Mode", color = primaryText, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Tap to switch theme", fontSize = 12.sp, color = secondaryText)
                    }
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { themeViewModel.setDarkMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF1E8449)
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(Routes.Login.route) { popUpTo(0) { inclusive = true } }
            },
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun ProfileRow(
    label: String,
    value: String,
    icon: ImageVector,
    primaryColor: Color,
    secondaryColor: Color,
    lineColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = secondaryColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, fontSize = 12.sp, color = secondaryColor)
                Text(value, fontSize = 15.sp, color = primaryColor, fontWeight = FontWeight.Medium)
            }
        }
        Divider(color = lineColor)
    }
}
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
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.serah.hustlescore.navigation.Routes

@Composable
fun UserProfileScreen(navController: NavHostController) {

    // ✅ FIX: load real data from Firebase instead of static placeholders
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

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
            .getReference("users/$uid/profile")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    @Suppress("UNCHECKED_CAST")
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
            .background(Color(0xFFF4F6F9))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Header banner ──────────────────────────────────────────────
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

        // ── Edit button ────────────────────────────────────────────────
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

        // ── Info card ──────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Personal Details", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    color = Color(0xFF1E8449), modifier = Modifier.padding(bottom = 8.dp))
                ProfileRow("Phone",      phone,      Icons.Default.Phone)
                ProfileRow("County",     county,     Icons.Default.LocationOn)
                ProfileRow("ID Number",  "••••••••", Icons.Default.CreditCard)  // masked for privacy
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Financial Details", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    color = Color(0xFF1E8449), modifier = Modifier.padding(bottom = 8.dp))
                ProfileRow("Occupation",    occupation, Icons.Default.Work)
                ProfileRow("Employer",      employer,   Icons.Default.Business)
                ProfileRow("Monthly Income","KES $income", Icons.Default.AttachMoney)
            }
        }

        Spacer(Modifier.height(12.dp))

        if (bio.isNotBlank() && bio != "—") {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                        color = Color(0xFF1E8449), modifier = Modifier.padding(bottom = 8.dp))
                    Text(bio, fontSize = 14.sp, color = Color(0xFF374151), lineHeight = 20.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // ── Sign out ───────────────────────────────────────────────────
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
fun ProfileRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF9CA3AF))
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
    Divider(color = Color(0xFFF3F4F6))
}
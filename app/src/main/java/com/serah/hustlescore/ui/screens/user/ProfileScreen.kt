package com.serah.hustlescore.ui.screens.user

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hustlescore.ui.theme.*
import com.serah.hustlescore.ui.theme.BackgroundGray
import com.serah.hustlescore.ui.theme.HustleGreen
import com.serah.hustlescore.ui.theme.TextSecondary

@Composable
fun ProfileScreen(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var editing by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }
    var county by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    val uid = currentUser?.uid ?: ""
    val initials = currentUser?.displayName?.split(" ")?.mapNotNull { it.firstOrNull()?.toString() }?.take(2)?.joinToString("") ?: "U"

    LaunchedEffect(Unit) {
        FirebaseDatabase.getInstance().getReference("users/$uid")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    phone = snapshot.child("phone").getValue(String::class.java) ?: ""
                    county = snapshot.child("county").getValue(String::class.java) ?: ""
                    occupation = snapshot.child("occupation").getValue(String::class.java) ?: ""
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Profile & Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        // Avatar Card
        Card(shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(8.dp)) {
            Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(HustleGreen, Color(0xFF145A32)))).padding(32.dp)) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.2f)).border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center) {
                        Text(initials, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(currentUser?.displayName ?: "User", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(currentUser?.email ?: "", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = Color.White.copy(alpha = 0.2f)) {
                        Text("Member", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        // Personal Details
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Personal Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    TextButton(onClick = { editing = !editing }) { Text(if (editing) "Cancel" else "Edit", color = HustleGreen) }
                }
                Spacer(Modifier.height(12.dp))

                if (editing) {
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), singleLine = true)
                    OutlinedTextField(value = county, onValueChange = { county = it }, label = { Text("County") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), singleLine = true)
                    OutlinedTextField(value = occupation, onValueChange = { occupation = it }, label = { Text("Occupation") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), singleLine = true)
                    Button(
                        onClick = {
                            saving = true
                            FirebaseDatabase.getInstance().getReference("users/$uid").updateChildren(mapOf("phone" to phone, "county" to county, "occupation" to occupation))
                            saving = false
                            editing = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HustleGreen)
                    ) {
                        if (saving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        else { Icon(Icons.Default.Save, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Save Changes") }
                    }
                } else {
                    ProfileRow(label = "Phone", value = phone.ifBlank { "Not set" }, icon = Icons.Default.Phone)
                    ProfileRow(label = "County", value = county.ifBlank { "Not set" }, icon = Icons.Default.LocationOn)
                    ProfileRow(label = "Occupation", value = occupation.ifBlank { "Not set" }, icon = Icons.Default.Work)
                    ProfileRow(label = "Email", value = currentUser?.email ?: "", icon = Icons.Default.Email)
                }
            }
        }

        // Sign Out
        Button(
            onClick = { FirebaseAuth.getInstance().signOut() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Sign Out")
        }
    }
}

@Composable
fun ProfileRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = HustleGreen, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 10.sp, color = TextSecondary)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
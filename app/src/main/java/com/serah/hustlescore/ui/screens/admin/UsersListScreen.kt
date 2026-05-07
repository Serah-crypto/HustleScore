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
import com.google.firebase.database.*
import com.hustlescore.ui.theme.HustleScoreTheme
import com.serah.hustlescore.models.AppUser
import com.serah.hustlescore.navigation.Routes
import com.serah.hustlescore.ui.theme.BackgroundGray
import com.serah.hustlescore.ui.theme.HustleGreen
import com.serah.hustlescore.ui.theme.TextSecondary

@Composable
fun UsersListScreen(navController: NavController) {
    var users by remember { mutableStateOf<List<AppUser>>(emptyList()) }
    var search by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseDatabase.getInstance().getReference("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    users = snapshot.children.mapNotNull { it.getValue(AppUser::class.java) }
                    loading = false
                }
                override fun onCancelled(error: DatabaseError) { loading = false }
            })
    }

    val filtered = users.filter { it.fullName.contains(search, ignoreCase = true) || it.email.contains(search, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Users", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("${users.size} registered users", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name or email...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
        }

        if (loading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = HustleGreen) }
        else LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            itemsIndexed(filtered) { _, user ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Routes.UserDetail.createRoute(user.id)) },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(HustleGreen), contentAlignment = Alignment.Center) {
                            Text((user.fullName.firstOrNull() ?: user.email.firstOrNull() ?: 'U').toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(user.fullName.ifBlank { "Unknown" }, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(user.email, fontSize = 12.sp, color = TextSecondary)
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = HustleGreen.copy(alpha = 0.1f)) {
                            Text(user.role.ifBlank { "user" }, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 11.sp, color = HustleGreen)
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                    }
                }
            }
            if (filtered.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(48.dp), tint = TextSecondary)
                            Spacer(Modifier.height(8.dp))
                            Text("No users found", color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UsersListScreenPreview() {
    HustleScoreTheme {   // Replace with your actual Theme name if different
        UsersListScreen(navController = rememberNavController())
    }
}
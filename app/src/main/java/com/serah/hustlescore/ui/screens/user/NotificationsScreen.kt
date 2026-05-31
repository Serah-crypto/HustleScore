// ════════════════════════════════════════════════════════════════════════════
// NotificationsScreen.kt
// ════════════════════════════════════════════════════════════════════════════
package com.serah.hustlescore.ui.screens.user

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.serah.hustlescore.models.AppNotification
import com.serah.hustlescore.ui.theme.ThemeViewModel

@Composable
fun NotificationsScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()

    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF4F6F9)
    val cardColor       = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val headerColor     = if (isDarkMode) Color(0xFF1A1A1A) else Color.White
    val primaryText     = if (isDarkMode) Color.White       else Color(0xFF111827)
    val secondaryText   = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val unreadDotBg     = if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFE5E7EB)
    val HustleGreen     = Color(0xFF1E8449)

    var notifications by remember { mutableStateOf<List<AppNotification>>(emptyList()) }
    var loading       by remember { mutableStateOf(true) }
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(Unit) {
        if (uid.isBlank()) { loading = false; return@LaunchedEffect }
        FirebaseDatabase.getInstance().getReference("notifications/$uid")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    notifications = snapshot.children.mapNotNull { child ->
                        val map = child.getValue(object : com.google.firebase.database.GenericTypeIndicator<Map<String, Any>>() {}) ?: return@mapNotNull null
                        AppNotification(
                            id        = child.key ?: "",
                            title     = map["title"]     as? String  ?: "",
                            message   = map["message"]   as? String  ?: "",
                            type      = map["type"]      as? String  ?: "system",
                            isRead    = map["isRead"]    as? Boolean ?: false,
                            createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L
                        )
                    }.sortedByDescending { it.createdAt }
                    loading = false
                }
                override fun onCancelled(error: DatabaseError) { loading = false }
            })
    }

    val unreadCount = notifications.count { !it.isRead }

    Column(modifier = Modifier.fillMaxSize().background(backgroundColor)) {

        // Header
        Row(modifier = Modifier.fillMaxWidth().background(headerColor).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Notifications", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = primaryText)
                    if (unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(10.dp), color = HustleGreen) {
                            Text("$unreadCount", Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text("${notifications.size} total notifications", fontSize = 13.sp, color = secondaryText)
            }
            if (unreadCount > 0) {
                TextButton(onClick = {
                    val db = FirebaseDatabase.getInstance().getReference("notifications/$uid")
                    notifications.filter { !it.isRead }.forEach { n -> db.child(n.id).child("isRead").setValue(true) }
                }) { Text("Mark All Read", fontSize = 12.sp, color = HustleGreen) }
            }
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = HustleGreen) }

            notifications.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)).background(unreadDotBg), Alignment.Center) {
                            Icon(Icons.Default.Notifications, null, modifier = Modifier.size(32.dp), tint = secondaryText)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("All caught up!", fontWeight = FontWeight.SemiBold, color = secondaryText)
                        Spacer(Modifier.height(4.dp))
                        Text("No notifications yet.", fontSize = 13.sp, color = secondaryText)
                    }
                }
            }

            else -> {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(notifications) { _, notif ->
                        val (iconRes, bgColor) = when (notif.type) {
                            "score_update" -> Icons.Default.TrendingUp to Color(0xFFDCFCE7)
                            "tip"          -> Icons.Default.Lightbulb  to Color(0xFFFEF3C7)
                            "alert"        -> Icons.Default.Warning     to Color(0xFFFEE2E2)
                            else           -> Icons.Default.Info        to Color(0xFFDBEAFE)
                        }
                        Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(cardColor),
                            border = if (!notif.isRead) BorderStroke(2.dp, HustleGreen) else null,
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (!notif.isRead) FirebaseDatabase.getInstance()
                                    .getReference("notifications/$uid/${notif.id}/isRead").setValue(true)
                            }) {
                            Row(modifier = Modifier.padding(14.dp)) {
                                Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(bgColor), Alignment.Center) {
                                    Icon(iconRes, null, modifier = Modifier.size(20.dp), tint = Color(0xFF374151))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(notif.title, fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp, color = primaryText, modifier = Modifier.weight(1f))
                                        if (!notif.isRead) Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(HustleGreen))
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(notif.message, fontSize = 12.sp, color = secondaryText, lineHeight = 17.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(notif.createdAt)),
                                        fontSize = 10.sp, color = secondaryText)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
package com.serah.hustlescore.components



import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serah.hustlescore.util.SmsPermissionHelper

@Composable
fun SmsPermissionBanner() {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(SmsPermissionHelper.hasPermission(context))
    }

    AnimatedVisibility(visible = !hasPermission) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Sms,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Enable Auto SMS Detection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF92400E)
                    )
                    Text(
                        "Allow HustleScore to auto-read M-Pesa SMS",
                        fontSize = 11.sp,
                        color = Color(0xFFB45309)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        SmsPermissionHelper.requestPermission(context as Activity)
                        hasPermission = SmsPermissionHelper.hasPermission(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Enable", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

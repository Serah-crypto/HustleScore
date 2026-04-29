package com.serah.hustlescore.ui.screens.auth

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
import com.hustlescore.ui.theme.*
import com.serah.hustlescore.ui.theme.BackgroundGray
import com.serah.hustlescore.ui.theme.HustleGreen
import com.serah.hustlescore.ui.theme.TextSecondary

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundGray).verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(HustleGreen, Color(0xFF145A32)))),
            contentAlignment = Alignment.Center) {
            Text("H", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(16.dp))
        Text("HustleScore", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))

        Card(shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (sent) "Check Your Email" else "Forgot Password?", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    if (sent) "We sent a reset link to $email" else "Enter your email to receive a reset link",
                    fontSize = 13.sp, color = TextSecondary
                )
                Spacer(Modifier.height(20.dp))

                if (sent) {
                    Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(32.dp)).background(Color(0xFFDCFCE7)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(36.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { sent = false }) { Text("Try Again") }
                    Spacer(Modifier.height(8.dp))
                    TextButton(modifier = Modifier.fillMaxWidth(), onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Back to App", color = TextSecondary)
                    }
                } else {
                    OutlinedTextField(
                        value = email, onValueChange = { email = it; error = "" },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        isError = error.isNotEmpty()
                    )
                    if (error.isNotEmpty()) Text(error, color = Color(0xFFDC2626), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (email.isBlank()) { error = "Please enter your email"; return@Button }
                            loading = true
                            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                .addOnCompleteListener { task ->
                                    loading = false
                                    if (task.isSuccessful) sent = true
                                    else error = task.exception?.message ?: "Failed to send email"
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HustleGreen),
                        enabled = !loading
                    ) {
                        if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        else { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Send Reset Link") }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(modifier = Modifier.fillMaxWidth(), onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Back", color = TextSecondary)
                    }
                }
            }
        }
    }
}
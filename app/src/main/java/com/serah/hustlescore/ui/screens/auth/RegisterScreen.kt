package com.serah.hustlescore.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext           // FIX 1: added
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.serah.hustlescore.data.AuthState
import com.serah.hustlescore.data.AuthViewModel
import com.serah.hustlescore.navigation.Routes                // FIX 2: use Routes not Screen
import com.serah.hustlescore.navigation.Screen
import com.serah.hustlescore.ui.theme.HustleGreen
import com.serah.hustlescore.ui.theme.TextPrimary
import com.serah.hustlescore.ui.theme.TextSecondary

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    onRegisterSuccess: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current                        // FIX 1: was missing
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (val state = authState) {// In RegisterScreen & LoginScreen LaunchedEffect:
            is AuthState.Registered -> {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }  // FIX 5: clear full back stack
                }
                authViewModel.resetState()
            }

            is AuthState.LoggedIn -> {
                val dest = if (state.role == "admin") Screen.AdminDashboard.route
                else Screen.Dashboard.route
                navController.navigate(dest) {
                    popUpTo(0) { inclusive = true }  // FIX 5: user can't back-navigate to login
                }
                authViewModel.resetState()
            }

            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetState()                   // FIX 3: same
            }
            else -> {}
        }
    }

    val passwordStrength = when {
        password.isEmpty() -> null
        password.length >= 10 && password.any { it.isDigit() } && password.any { it.isUpperCase() } ->
            Triple("Strong", Color(0xFF16A34A), 1f)
        password.length >= 6 -> Triple("Moderate", Color(0xFFF59E0B), 0.6f)
        else -> Triple("Weak", Color(0xFFDC2626), 0.3f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F4C2A), Color(0xFF1E8449), Color(0xFFF4F6F9))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("H", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(12.dp))
            Text("HustleScore", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("Create your free account", color = Color.White.copy(alpha = 0.75f), fontSize = 14.sp)
            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Text("Join HustleScore 🚀", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Build your alternative credit profile", fontSize = 14.sp, color = TextSecondary)
                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = HustleGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HustleGreen,
                            focusedLabelColor = HustleGreen,
                            cursorColor = HustleGreen
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = HustleGreen) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HustleGreen,
                            focusedLabelColor = HustleGreen,
                            cursorColor = HustleGreen
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = HustleGreen) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HustleGreen,
                            focusedLabelColor = HustleGreen,
                            cursorColor = HustleGreen
                        )
                    )

                    passwordStrength?.let { (label, color, fraction) ->
                        LinearProgressIndicator(
                            progress = { fraction },           // FIX 4: lambda form avoids deprecation
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .height(4.dp),
                            color = color,
                            trackColor = Color(0xFFE5E7EB)
                        )
                        Text(label, fontSize = 11.sp, color = color, modifier = Modifier.padding(top = 2.dp))
                    }

                    Spacer(Modifier.height(14.dp))

                    val passwordsMismatch = confirmPassword.isNotEmpty() && password != confirmPassword
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        isError = passwordsMismatch,
                        supportingText = {                     // FIX 5: show mismatch message
                            if (passwordsMismatch) Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HustleGreen,
                            focusedLabelColor = HustleGreen,
                            cursorColor = HustleGreen
                        )
                    )

                    Spacer(Modifier.height(20.dp))

                    // Error Banner
                    AnimatedVisibility(visible = authState is AuthState.Error) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEE2E2)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = (authState as? AuthState.Error)?.message.orEmpty(),
                                    color = Color(0xFFDC2626),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { authViewModel.register(fullName, email, password, confirmPassword) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HustleGreen),
                        enabled = authState !is AuthState.Loading && !passwordsMismatch  // FIX 6: block submit on mismatch
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                        } else {
                            Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    TextButton(
                        onClick = { navController.navigate(Routes.Login.route) {
                            popUpTo(0) { inclusive = true }}},
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Already have an account? Sign In", color = HustleGreen)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Text(
                "🔒 Your data is private and secure",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
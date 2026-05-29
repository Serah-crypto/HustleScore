package com.hustlescore.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.serah.hustlescore.data.AuthState
import com.serah.hustlescore.data.AuthViewModel
import com.serah.hustlescore.navigation.Routes

// ── Palette (matches SplashScreen / OnboardingScreen) ────────────────────────
private val DeepGreen     = Color(0xFF011710)
private val MidGreen      = Color(0xFF022C18)
private val BrightGreen   = Color(0xFF16A34A)
private val LimeGreen     = Color(0xFF4ADE80)
private val EmeraldAccent = Color(0xFF34D399)
private val Gold          = Color(0xFFFBBF24)
private val GoldLight     = Color(0xFFFDE68A)
private val White         = Color.White
private val GlassWhite    = Color.White.copy(alpha = 0.07f)
private val GlassBorder   = Color.White.copy(alpha = 0.12f)

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: (role: String) -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {}
) {
    var email          by remember { mutableStateOf("") }
    var password       by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailFocused   by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val context      = LocalContext.current
    val authState    by authViewModel.authState.collectAsState()

    // ── Entrance animations ───────────────────────────────────────────────
    val logoAlpha   = remember { Animatable(0f) }
    val logoScale   = remember { Animatable(0.7f) }
    val cardAlpha   = remember { Animatable(0f) }
    val cardSlide   = remember { Animatable(40f) }
    val bottomAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(500))
        logoScale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
        cardAlpha.animateTo(1f, tween(400))
        cardSlide.animateTo(0f, tween(400, easing = EaseOutCubic))
        bottomAlpha.animateTo(1f, tween(350))
    }

    // ── Shimmer for CTA button ────────────────────────────────────────────
    val infinite = rememberInfiniteTransition(label = "login")
    val shimmer by infinite.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing)),
        label = "shimmer"
    )
    val breathe by infinite.animateFloat(
        initialValue = 0.97f, targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(2800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "breathe"
    )

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Registered -> {
                navController.navigate(Routes.UserDetailForm.route) { popUpTo(0) { inclusive = true } }
                authViewModel.resetState()
            }
            is AuthState.LoggedIn -> {
                val dest = if (state.role == "admin") Routes.AdminDashboard.route else Routes.Home.route
                navController.navigate(dest) { popUpTo(0) { inclusive = true } }
                authViewModel.resetState()
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f   to DeepGreen,
                    0.35f to MidGreen,
                    0.72f to Color(0xFF034D25),
                    1f   to Color(0xFF022820)
                )
            )
    ) {

        // ── Atmospheric glows ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(360.dp)
                .align(Alignment.TopEnd)
                .offset(x = 110.dp, y = (-70).dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(BrightGreen.copy(0.14f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = 60.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Gold.copy(0.10f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterStart)
                .offset(x = (-60).dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(LimeGreen.copy(0.05f), Color.Transparent)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(60.dp))

            // ── Logo ──────────────────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(130.dp)
                    .alpha(logoAlpha.value)
                    .scale(logoScale.value)
            ) {
                // Pulsing halo
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(breathe)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(LimeGreen.copy(0.14f), Color.Transparent)))
                )
                // Gold ring
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .border(
                            1.dp,
                            Brush.sweepGradient(
                                listOf(Gold.copy(0f), Gold.copy(0.55f), GoldLight, Gold.copy(0.55f), Gold.copy(0f))
                            ),
                            CircleShape
                        )
                )
                // Logo card
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .shadow(24.dp, RoundedCornerShape(22.dp),
                            ambientColor = LimeGreen.copy(0.35f),
                            spotColor = Gold.copy(0.30f))
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                0f   to Color(0xFF023D20),
                                0.4f to Color(0xFF16A34A),
                                1f   to Color(0xFF34D399)
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.linearGradient(
                                listOf(Gold.copy(0.65f), EmeraldAccent.copy(0.25f), Gold.copy(0.45f))
                            ),
                            RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "HS",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = White,
                        letterSpacing = (-1.5).sp,
                        style = TextStyle(shadow = Shadow(Color(0xFF011710).copy(0.60f), blurRadius = 12f))
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // App name
            Text(
                text = "HustleScore",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = White,
                letterSpacing = (-0.8).sp,
                style = TextStyle(shadow = Shadow(LimeGreen.copy(0.20f), blurRadius = 16f)),
                modifier = Modifier.alpha(logoAlpha.value)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Credit Intelligence for Everyone",
                fontSize = 13.sp,
                color = White.copy(0.55f),
                letterSpacing = 0.3.sp,
                modifier = Modifier.alpha(logoAlpha.value)
            )

            Spacer(Modifier.height(36.dp))

            // ── Glass card ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = cardSlide.value.dp)
                    .alpha(cardAlpha.value)
                    .shadow(
                        40.dp, RoundedCornerShape(28.dp),
                        ambientColor = BrightGreen.copy(0.12f),
                        spotColor = BrightGreen.copy(0.10f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF023D20).copy(0.95f), Color(0xFF011F12).copy(0.98f))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(EmeraldAccent.copy(0.30f), Color.Transparent, Gold.copy(0.12f))
                        ),
                        RoundedCornerShape(28.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(28.dp)) {

                    // Card header
                    Text(
                        text = "Welcome back 👋",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = White,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Sign in to your account",
                        fontSize = 13.sp,
                        color = White.copy(0.50f),
                        letterSpacing = 0.2.sp
                    )

                    Spacer(Modifier.height(28.dp))

                    // ── Email field ───────────────────────────────────────
                    GlassTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email, null,
                                tint = if (emailFocused) LimeGreen else White.copy(0.45f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        isFocused = emailFocused,
                        onFocusChange = { emailFocused = it }
                    )

                    Spacer(Modifier.height(6.dp))

                    // ── Password field ────────────────────────────────────
                    GlassTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock, null,
                                tint = if (passwordFocused) LimeGreen else White.copy(0.45f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = White.copy(0.45f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            authViewModel.login(email, password)
                        }),
                        isFocused = passwordFocused,
                        onFocusChange = { passwordFocused = it }
                    )

                    // Forgot password
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(onClick = { navController.navigate(Routes.ForgotPassword.route) }) {
                            Text(
                                text = "Forgot Password?",
                                color = Gold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }

                    // ── Error banner ──────────────────────────────────────
                    AnimatedVisibility(visible = authState is AuthState.Error) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF7F1D1D).copy(0.50f))
                                .border(1.dp, Color(0xFFDC2626).copy(0.40f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.ErrorOutline, null,
                                    tint = Color(0xFFFCA5A5),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = (authState as? AuthState.Error)?.message.orEmpty(),
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // ── Sign In button ────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(
                                16.dp, RoundedCornerShape(16.dp),
                                ambientColor = BrightGreen.copy(0.45f),
                                spotColor = LimeGreen.copy(0.35f)
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    0f to BrightGreen,
                                    shimmer.coerceIn(0f, 1f) to LimeGreen.copy(0.85f),
                                    1f to Color(0xFF15803D)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { authViewModel.login(email, password) },
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                            enabled = authState !is AuthState.Loading
                        ) {
                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = White,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Icon(Icons.Default.Login, null, modifier = Modifier.size(18.dp), tint = White)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "Sign In",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = White,
                                    letterSpacing = 0.3.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Divider ───────────────────────────────────────────
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, White.copy(0.15f))
                                    )
                                )
                        )
                        Text(
                            text = "  or  ",
                            color = White.copy(0.35f),
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(White.copy(0.15f), Color.Transparent)
                                    )
                                )
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    // ── Sign up row ───────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Don't have an account?",
                            color = White.copy(0.50f),
                            fontSize = 13.sp
                        )
                        TextButton(onClick = { navController.navigate(Routes.Register.route) }) {
                            Text(
                                text = "Sign Up",
                                color = LimeGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                style = TextStyle(shadow = Shadow(LimeGreen.copy(0.40f), blurRadius = 8f))
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Bottom trust badge ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .alpha(bottomAlpha.value)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // dot · line · dot
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(3.dp).clip(CircleShape).background(Gold.copy(0.40f)))
                    Box(
                        Modifier.width(28.dp).height(1.dp).background(
                            Brush.horizontalGradient(listOf(Color.Transparent, Gold.copy(0.35f), Color.Transparent))
                        )
                    )
                    Box(Modifier.size(3.dp).clip(CircleShape).background(Gold.copy(0.40f)))
                }
                Text(
                    text = "🔒  Your data is private and secure",
                    color = White.copy(0.38f),
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable glass-style text field
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isFocused: Boolean = false,
    onFocusChange: (Boolean) -> Unit = {}
) {
    val borderBrush = if (isFocused)
        Brush.linearGradient(listOf(LimeGreen.copy(0.70f), EmeraldAccent.copy(0.40f)))
    else
        Brush.linearGradient(listOf(White.copy(0.12f), White.copy(0.06f)))

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                fontSize = 12.sp,
                color = if (isFocused) LimeGreen.copy(0.90f) else White.copy(0.45f)
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { onFocusChange(it.isFocused) },
        shape = RoundedCornerShape(14.dp),
        textStyle = TextStyle(
            color = White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor   = LimeGreen.copy(0.06f),
            unfocusedContainerColor = White.copy(0.05f),
            cursorColor = LimeGreen,
            focusedLabelColor   = LimeGreen,
            unfocusedLabelColor = White.copy(0.45f),
        )
    )
    // Animated border overlay
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .offset(y = (-56).dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(14.dp)
            )
    )
    Spacer(Modifier.height((-0).dp)) // compensates the negative offset
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}
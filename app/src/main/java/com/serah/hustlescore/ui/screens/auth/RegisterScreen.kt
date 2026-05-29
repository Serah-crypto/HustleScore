package com.serah.hustlescore.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.serah.hustlescore.data.AuthState
import com.serah.hustlescore.data.AuthViewModel
import com.serah.hustlescore.navigation.Routes

// ── Palette ───────────────────────────────────────────────────────────────────
private val RDeepGreen     = Color(0xFF011710)
private val RMidGreen      = Color(0xFF022C18)
private val RBrightGreen   = Color(0xFF16A34A)
private val RLimeGreen     = Color(0xFF4ADE80)
private val REmeraldAccent = Color(0xFF34D399)
private val RGold          = Color(0xFFFBBF24)
private val RGoldLight     = Color(0xFFFDE68A)
private val RWhite         = Color.White

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    onRegisterSuccess: () -> Unit = {}
) {
    var fullName         by remember { mutableStateOf("") }
    var email            by remember { mutableStateOf("") }
    var password         by remember { mutableStateOf("") }
    var confirmPassword  by remember { mutableStateOf("") }
    var passwordVisible  by remember { mutableStateOf(false) }

    var fullNameFocused  by remember { mutableStateOf(false) }
    var emailFocused     by remember { mutableStateOf(false) }
    var passwordFocused  by remember { mutableStateOf(false) }
    var confirmFocused   by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val context      = LocalContext.current
    val authState    by authViewModel.authState.collectAsState()

    // ── Entrance animations ───────────────────────────────────────────────
    val logoAlpha  = remember { Animatable(0f) }
    val logoScale  = remember { Animatable(0.7f) }
    val cardAlpha  = remember { Animatable(0f) }
    val cardSlide  = remember { Animatable(40f) }
    val btmAlpha   = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(500))
        logoScale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
        cardAlpha.animateTo(1f, tween(400))
        cardSlide.animateTo(0f, tween(400, easing = EaseOutCubic))
        btmAlpha.animateTo(1f, tween(350))
    }

    val infinite = rememberInfiniteTransition(label = "reg")
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

    val passwordsMismatch = confirmPassword.isNotEmpty() && password != confirmPassword
    val passwordStrength = when {
        password.isEmpty() -> null
        password.length >= 10 && password.any { it.isDigit() } && password.any { it.isUpperCase() } ->
            Triple("Strong", RLimeGreen, 1f)
        password.length >= 6 -> Triple("Moderate", RGold, 0.6f)
        else -> Triple("Weak", Color(0xFFDC2626), 0.3f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f    to RDeepGreen,
                    0.35f to RMidGreen,
                    0.72f to Color(0xFF034D25),
                    1f    to Color(0xFF022820)
                )
            )
    ) {

        // ── Atmospheric glows ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(360.dp).align(Alignment.TopEnd)
                .offset(x = 110.dp, y = (-70).dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf(RBrightGreen.copy(0.14f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .size(240.dp).align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = 60.dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf(RGold.copy(0.10f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .size(180.dp).align(Alignment.CenterEnd)
                .offset(x = 60.dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf(RLimeGreen.copy(0.05f), Color.Transparent)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(52.dp))

            // ── Logo ──────────────────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp).alpha(logoAlpha.value).scale(logoScale.value)
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp).scale(breathe).clip(CircleShape)
                        .background(Brush.radialGradient(listOf(RLimeGreen.copy(0.13f), Color.Transparent)))
                )
                Box(
                    modifier = Modifier.size(86.dp).clip(CircleShape)
                        .border(
                            1.dp,
                            Brush.sweepGradient(
                                listOf(RGold.copy(0f), RGold.copy(0.55f), RGoldLight, RGold.copy(0.55f), RGold.copy(0f))
                            ),
                            CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(24.dp, RoundedCornerShape(20.dp),
                            ambientColor = RLimeGreen.copy(0.35f),
                            spotColor = RGold.copy(0.30f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                0f to Color(0xFF023D20), 0.4f to RBrightGreen, 1f to REmeraldAccent
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.linearGradient(listOf(RGold.copy(0.65f), REmeraldAccent.copy(0.25f), RGold.copy(0.45f))),
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "HS",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = RWhite,
                        letterSpacing = (-1.5).sp,
                        style = TextStyle(shadow = Shadow(Color(0xFF011710).copy(0.60f), blurRadius = 12f))
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "HustleScore",
                fontSize = 28.sp, fontWeight = FontWeight.Black, color = RWhite,
                letterSpacing = (-0.8).sp,
                style = TextStyle(shadow = Shadow(RLimeGreen.copy(0.20f), blurRadius = 16f)),
                modifier = Modifier.alpha(logoAlpha.value)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "Create your free account",
                fontSize = 13.sp, color = RWhite.copy(0.55f), letterSpacing = 0.3.sp,
                modifier = Modifier.alpha(logoAlpha.value)
            )

            Spacer(Modifier.height(28.dp))

            // ── Glass card ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth().padding(horizontal = 20.dp)
                    .offset(y = cardSlide.value.dp).alpha(cardAlpha.value)
                    .shadow(40.dp, RoundedCornerShape(28.dp),
                        ambientColor = RBrightGreen.copy(0.12f),
                        spotColor = RBrightGreen.copy(0.10f))
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF023D20).copy(0.95f), Color(0xFF011F12).copy(0.98f))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(REmeraldAccent.copy(0.30f), Color.Transparent, RGold.copy(0.12f))
                        ),
                        RoundedCornerShape(28.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    Text(
                        "Join HustleScore 🚀",
                        fontSize = 21.sp, fontWeight = FontWeight.Black,
                        color = RWhite, letterSpacing = (-0.3).sp
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Build your alternative credit profile",
                        fontSize = 13.sp, color = RWhite.copy(0.50f), letterSpacing = 0.2.sp
                    )

                    Spacer(Modifier.height(20.dp))

                    // ── Full Name ─────────────────────────────────────────
                    RegGlassTextField(
                        value = fullName, onValueChange = { fullName = it },
                        label = "Full Name",
                        leadingIcon = {
                            Icon(Icons.Default.Person, null,
                                tint = if (fullNameFocused) RLimeGreen else RWhite.copy(0.45f),
                                modifier = Modifier.size(20.dp))
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        isFocused = fullNameFocused, onFocusChange = { fullNameFocused = it }
                    )

                    Spacer(Modifier.height(6.dp))

                    // ── Email ─────────────────────────────────────────────
                    RegGlassTextField(
                        value = email, onValueChange = { email = it },
                        label = "Email Address",
                        leadingIcon = {
                            Icon(Icons.Default.Email, null,
                                tint = if (emailFocused) RLimeGreen else RWhite.copy(0.45f),
                                modifier = Modifier.size(20.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        isFocused = emailFocused, onFocusChange = { emailFocused = it }
                    )

                    Spacer(Modifier.height(6.dp))

                    // ── Password ──────────────────────────────────────────
                    RegGlassTextField(
                        value = password, onValueChange = { password = it },
                        label = "Password",
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null,
                                tint = if (passwordFocused) RLimeGreen else RWhite.copy(0.45f),
                                modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null, tint = RWhite.copy(0.45f), modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        isFocused = passwordFocused, onFocusChange = { passwordFocused = it }
                    )

                    // ── Password strength bar ─────────────────────────────
                    passwordStrength?.let { (label, color, fraction) ->
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth().height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(RWhite.copy(0.10f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction).fillMaxHeight()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        Brush.horizontalGradient(listOf(color.copy(0.70f), color))
                                    )
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(label, fontSize = 11.sp, color = color, letterSpacing = 0.3.sp)
                    }

                    Spacer(Modifier.height(6.dp))

                    // ── Confirm Password ──────────────────────────────────
                    RegGlassTextField(
                        value = confirmPassword, onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null,
                                tint = if (confirmFocused) RLimeGreen else RWhite.copy(0.45f),
                                modifier = Modifier.size(20.dp))
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        isFocused = confirmFocused, onFocusChange = { confirmFocused = it },
                        isError = passwordsMismatch
                    )
                    if (passwordsMismatch) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "Passwords do not match",
                            fontSize = 11.sp, color = Color(0xFFFCA5A5), letterSpacing = 0.2.sp
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    // ── Error banner ──────────────────────────────────────
                    AnimatedVisibility(visible = authState is AuthState.Error) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth().padding(bottom = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF7F1D1D).copy(0.50f))
                                .border(1.dp, Color(0xFFDC2626).copy(0.40f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ErrorOutline, null,
                                    tint = Color(0xFFFCA5A5), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = (authState as? AuthState.Error)?.message.orEmpty(),
                                    color = Color(0xFFFCA5A5), fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // ── Create Account button ─────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth().height(54.dp)
                            .shadow(16.dp, RoundedCornerShape(16.dp),
                                ambientColor = RBrightGreen.copy(0.45f),
                                spotColor = RLimeGreen.copy(0.35f))
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    0f to RBrightGreen,
                                    shimmer.coerceIn(0f, 1f) to RLimeGreen.copy(0.85f),
                                    1f to Color(0xFF15803D)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { authViewModel.register(fullName, email, password, confirmPassword) },
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                            enabled = authState !is AuthState.Loading && !passwordsMismatch
                        ) {
                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = RWhite, strokeWidth = 2.5.dp
                                )
                            } else {
                                Icon(Icons.Default.PersonAdd, null,
                                    modifier = Modifier.size(18.dp), tint = RWhite)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Create Account", fontSize = 16.sp,
                                    fontWeight = FontWeight.Black, color = RWhite, letterSpacing = 0.3.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // ── Divider ───────────────────────────────────────────
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f).height(1.dp).background(
                            Brush.horizontalGradient(listOf(Color.Transparent, RWhite.copy(0.15f)))
                        ))
                        Text("  or  ", color = RWhite.copy(0.35f), fontSize = 12.sp, letterSpacing = 0.5.sp)
                        Box(Modifier.weight(1f).height(1.dp).background(
                            Brush.horizontalGradient(listOf(RWhite.copy(0.15f), Color.Transparent))
                        ))
                    }

                    Spacer(Modifier.height(14.dp))

                    // ── Sign in row ───────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Already have an account?", color = RWhite.copy(0.50f), fontSize = 13.sp)
                        TextButton(onClick = {
                            navController.navigate(Routes.Login.route) { popUpTo(0) { inclusive = true } }
                        }) {
                            Text(
                                "Sign In", color = RLimeGreen,
                                fontWeight = FontWeight.Black, fontSize = 13.sp,
                                style = TextStyle(shadow = Shadow(RLimeGreen.copy(0.40f), blurRadius = 8f))
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Bottom trust badge ────────────────────────────────────────
            Column(
                modifier = Modifier.alpha(btmAlpha.value).padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(3.dp).clip(CircleShape).background(RGold.copy(0.40f)))
                    Box(Modifier.width(28.dp).height(1.dp).background(
                        Brush.horizontalGradient(listOf(Color.Transparent, RGold.copy(0.35f), Color.Transparent))
                    ))
                    Box(Modifier.size(3.dp).clip(CircleShape).background(RGold.copy(0.40f)))
                }
                Text(
                    "🔒  Your data is private and secure",
                    color = RWhite.copy(0.38f), fontSize = 11.sp,
                    letterSpacing = 0.5.sp, textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Glass text field — same pattern as LoginScreen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RegGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isFocused: Boolean = false,
    onFocusChange: (Boolean) -> Unit = {},
    isError: Boolean = false
) {
    val borderBrush = when {
        isError   -> Brush.linearGradient(listOf(Color(0xFFDC2626).copy(0.70f), Color(0xFFFCA5A5).copy(0.40f)))
        isFocused -> Brush.linearGradient(listOf(RLimeGreen.copy(0.70f), REmeraldAccent.copy(0.40f)))
        else      -> Brush.linearGradient(listOf(RWhite.copy(0.12f), RWhite.copy(0.06f)))
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label, fontSize = 12.sp,
                color = when {
                    isError   -> Color(0xFFFCA5A5)
                    isFocused -> RLimeGreen.copy(0.90f)
                    else      -> RWhite.copy(0.45f)
                }
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        isError = isError,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { onFocusChange(it.isFocused) },
        shape = RoundedCornerShape(14.dp),
        textStyle = TextStyle(color = RWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = Color.Transparent,
            unfocusedBorderColor    = Color.Transparent,
            errorBorderColor        = Color.Transparent,
            focusedContainerColor   = RLimeGreen.copy(0.06f),
            unfocusedContainerColor = RWhite.copy(0.05f),
            errorContainerColor     = Color(0xFFDC2626).copy(0.08f),
            cursorColor             = RLimeGreen,
            focusedLabelColor       = RLimeGreen,
            unfocusedLabelColor     = RWhite.copy(0.45f),
            errorLabelColor         = Color(0xFFFCA5A5)
        )
    )
    Box(
        modifier = Modifier
            .fillMaxWidth().height(56.dp)
            .offset(y = (-56).dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isFocused || isError) 1.5.dp else 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(14.dp)
            )
    )
}
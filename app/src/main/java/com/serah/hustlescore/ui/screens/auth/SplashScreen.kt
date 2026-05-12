package com.serah.hustlescore.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.serah.hustlescore.navigation.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(navController: NavController) {

    val logoScale   = remember { Animatable(0f) }
    val logoAlpha   = remember { Animatable(0f) }
    val titleAlpha  = remember { Animatable(0f) }
    val titleSlide  = remember { Animatable(24f) }
    val tagAlpha    = remember { Animatable(0f) }
    val tagSlide    = remember { Animatable(16f) }
    val bottomAlpha = remember { Animatable(0f) }
    val barWidth    = remember { Animatable(0f) }

    val infinite = rememberInfiniteTransition(label = "i")
    val breathe by infinite.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "b"
    )
    val dotPulse by infinite.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "d"
    )

    LaunchedEffect(Unit) {
        // Logo bounces in
        launch {
            logoAlpha.animateTo(1f, tween(400))
            logoScale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
        }
        delay(500)
        // Title slides up
        launch {
            titleAlpha.animateTo(1f, tween(400))
            titleSlide.animateTo(0f, tween(400, easing = EaseOutCubic))
        }
        delay(200)
        // Gold bar grows
        launch { barWidth.animateTo(1f, tween(500, easing = EaseOutCubic)) }
        delay(200)
        // Tagline
        launch {
            tagAlpha.animateTo(1f, tween(400))
            tagSlide.animateTo(0f, tween(400, easing = EaseOutCubic))
        }
        delay(300)
        // Bottom
        launch { bottomAlpha.animateTo(1f, tween(400)) }

        delay(2200)
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val dest = if (user != null) "home" else "login"
        navController.navigate(dest) {
            popUpTo(Routes.SplashScreen.route) { inclusive = true }
        }
    }

    val deepGreen   = Color(0xFF022C18)
    val brightGreen = Color(0xFF16A34A)
    val limeGreen   = Color(0xFF4ADE80)
    val gold        = Color(0xFFFBBF24)
    val white       = Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF011A0D), deepGreen, Color(0xFF034D25), deepGreen)
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // Background glow top-right — drawn first (behind content)
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-60).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(brightGreen.copy(alpha = 0.12f), Color.Transparent)
                    )
                )
        )

        // Background glow bottom-left
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 50.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(gold.copy(alpha = 0.09f), Color.Transparent)
                    )
                )
        )

        // ── Main content — drawn LAST so it's always on top ──────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {

            // ── Logo ──────────────────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(150.dp)
            ) {
                // Pulsing halo behind logo
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(breathe)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(limeGreen.copy(alpha = 0.15f), Color.Transparent)
                            )
                        )
                )
                // Logo card
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .clip(RoundedCornerShape(26.dp))
                        .background(white),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "H",
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Black,
                        color = brightGreen
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── App name ──────────────────────────────────────────────────
            Text(
                text = "HustleScore",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = white,
                letterSpacing = (-0.5).sp,
                modifier = Modifier
                    .offset(y = titleSlide.value.dp)
                    .alpha(titleAlpha.value)
            )

            Spacer(Modifier.height(8.dp))

            // ── Gold bar ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth(barWidth.value * 0.45f)
                    .height(2.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, gold, gold, Color.Transparent)
                        )
                    )
            )

            Spacer(Modifier.height(12.dp))

            // ── Tagline ───────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = tagSlide.value.dp)
                    .alpha(tagAlpha.value)
            ) {
                Text(
                    text = "Know Your Score.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = white.copy(alpha = 0.90f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Own Your Future.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = limeGreen,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Feature pills ─────────────────────────────────────────────
            Row(
                modifier = Modifier.alpha(tagAlpha.value),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("📱 M-Pesa", "📊 Score", "🇰🇪 Kenya").forEach { pill ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(white.copy(alpha = 0.10f))
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = pill,
                            fontSize = 11.sp,
                            color = white.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            // ── Loading dots ──────────────────────────────────────────────
            Row(
                modifier = Modifier.alpha(bottomAlpha.value),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(0, 280, 560).forEachIndexed { i, delayMs ->
                    val p by rememberInfiniteTransition(label = "dot$i").animateFloat(
                        initialValue = 0.25f, targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            tween(650, delayMillis = delayMs, easing = EaseInOutSine),
                            RepeatMode.Reverse
                        ), label = "p$i"
                    )
                    Box(
                        modifier = Modifier
                            .size(if (i == 1) 11.dp else 7.dp)
                            .alpha(p)
                            .clip(CircleShape)
                            .background(if (i == 1) gold else limeGreen.copy(alpha = 0.75f))
                    )
                }
            }
        }

        // ── Bottom label — anchored, drawn last ───────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(bottomAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(1.dp)
                    .background(gold.copy(alpha = 0.40f))
            )
            Text(
                text = "Empowering Kenya's Informal Workers",
                fontSize = 11.sp,
                color = white.copy(alpha = 0.40f),
                letterSpacing = 0.6.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
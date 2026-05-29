package com.serah.hustlescore.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
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

    // Shimmer sweep across the gold bar
    val shimmerOffset by infinite.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )

    // Subtle ring pulse around logo card
    val ringPulse by infinite.animateFloat(
        initialValue = 0.30f, targetValue = 0.70f,
        animationSpec = infiniteRepeatable(tween(2400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "ring"
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
        val dest = if (user != null) "home" else "onboarding"
        navController.navigate(dest) {
            popUpTo(Routes.SplashScreen.route) { inclusive = true }
        }
    }

    // ── Colour palette ────────────────────────────────────────────────────
    val deepGreen      = Color(0xFF022C18)
    val midnightGreen  = Color(0xFF011710)   // slightly deeper base
    val brightGreen    = Color(0xFF16A34A)
    val limeGreen      = Color(0xFF4ADE80)
    val emeraldAccent  = Color(0xFF34D399)   // extra mid-tone for pill borders
    val gold           = Color(0xFFFBBF24)
    val goldLight      = Color(0xFFFDE68A)   // lighter shimmer highlight
    val white          = Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // Richer 5-stop gradient for more depth
                Brush.verticalGradient(
                    0.00f to midnightGreen,
                    0.20f to Color(0xFF011A0D),
                    0.50f to deepGreen,
                    0.78f to Color(0xFF034D25),
                    1.00f to Color(0xFF022820)
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // ── Atmospheric glow — top-right ──────────────────────────────────
        Box(
            modifier = Modifier
                .size(380.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-80).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(brightGreen.copy(alpha = 0.16f), Color.Transparent)
                    )
                )
        )

        // ── Atmospheric glow — bottom-left ────────────────────────────────
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = 60.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(gold.copy(alpha = 0.11f), Color.Transparent)
                    )
                )
        )

        // ── Subtle centre radial lift ─────────────────────────────────────
        Box(
            modifier = Modifier
                .size(500.dp)
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(brightGreen.copy(alpha = 0.05f), Color.Transparent)
                    )
                )
        )

        // ── Main content ──────────────────────────────────────────────────
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
                modifier = Modifier.size(160.dp)
            ) {
                // Outermost faint halo (pulsing)
                Box(
                    modifier = Modifier
                        .size(155.dp)
                        .scale(breathe * 0.97f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(limeGreen.copy(alpha = 0.08f), Color.Transparent)
                            )
                        )
                )
                // Inner glow halo
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .scale(breathe)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(limeGreen.copy(alpha = 0.18f), Color.Transparent)
                            )
                        )
                )
                // Gold ring accent — pulses opacity
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .alpha(logoAlpha.value * ringPulse)
                        .clip(CircleShape)
                        .border(
                            width = 1.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    gold.copy(alpha = 0f),
                                    gold.copy(alpha = 0.6f),
                                    goldLight,
                                    gold.copy(alpha = 0.6f),
                                    gold.copy(alpha = 0f)
                                )
                            ),
                            shape = CircleShape
                        )
                )
                // Logo card with elevation shadow
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .shadow(
                            elevation = 32.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = limeGreen.copy(alpha = 0.45f),
                            spotColor = gold.copy(alpha = 0.35f)
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            // Rich diagonal gradient: deep green → lime → emerald
                            Brush.linearGradient(
                                0f   to Color(0xFF023D20),
                                0.4f to Color(0xFF16A34A),
                                1f   to Color(0xFF34D399)
                            )
                        )
                        // Gold shimmer border
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    gold.copy(alpha = 0.70f),
                                    emeraldAccent.copy(alpha = 0.30f),
                                    gold.copy(alpha = 0.50f)
                                )
                            ),
                            shape = RoundedCornerShape(26.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "HS",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        color = white,
                        letterSpacing = (-1.5).sp,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color(0xFF011710).copy(alpha = 0.60f),
                                blurRadius = 12f
                            )
                        )
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── App name ──────────────────────────────────────────────────
            Text(
                text = "HustleScore",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = white,
                letterSpacing = (-1.0).sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = limeGreen.copy(alpha = 0.20f),
                        blurRadius = 18f
                    )
                ),
                modifier = Modifier
                    .offset(y = titleSlide.value.dp)
                    .alpha(titleAlpha.value)
            )

            Spacer(Modifier.height(10.dp))

            // ── Gold bar with shimmer ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth(barWidth.value * 0.45f)
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        // Shimmer: travelling light spot over gold
                        Brush.horizontalGradient(
                            0.0f                        to Color.Transparent,
                            (shimmerOffset - 0.4f).coerceIn(0f, 1f) to gold.copy(alpha = 0.5f),
                            shimmerOffset.coerceIn(0f, 1f)           to goldLight,
                            (shimmerOffset + 0.4f).coerceIn(0f, 1f) to gold.copy(alpha = 0.5f),
                            1.0f                        to Color.Transparent
                        )
                    )
            )

            Spacer(Modifier.height(14.dp))

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
                    color = white.copy(alpha = 0.88f),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.2.sp
                )
                Text(
                    text = "Own Your Future.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = limeGreen,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.2.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = limeGreen.copy(alpha = 0.45f),
                            blurRadius = 12f
                        )
                    )
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Feature pills — glassmorphism style ───────────────────────
            Row(
                modifier = Modifier.alpha(tagAlpha.value),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("📱 M-Pesa", "📊 Score", "🇰🇪 Kenya").forEach { pill ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        white.copy(alpha = 0.12f),
                                        white.copy(alpha = 0.06f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        emeraldAccent.copy(alpha = 0.35f),
                                        white.copy(alpha = 0.08f)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = pill,
                            fontSize = 11.sp,
                            color = white.copy(alpha = 0.90f),
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(52.dp))

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
                    val dotSize = when (i) {
                        1    -> 12.dp   // centre dot — slightly larger
                        else -> 7.dp
                    }
                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .alpha(p)
                            .shadow(
                                elevation = if (i == 1) 8.dp else 0.dp,
                                shape = CircleShape,
                                ambientColor = if (i == 1) gold else limeGreen,
                                spotColor   = if (i == 1) gold else limeGreen
                            )
                            .clip(CircleShape)
                            .background(
                                if (i == 1) Brush.radialGradient(listOf(goldLight, gold))
                                else Brush.radialGradient(
                                    listOf(limeGreen, limeGreen.copy(alpha = 0.70f))
                                )
                            )
                    )
                }
            }
        }

        // ── Bottom label — anchored ───────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .alpha(bottomAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Decorative dot-line-dot separator
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(gold.copy(alpha = 0.45f))
                )
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, gold.copy(alpha = 0.40f), Color.Transparent)
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(gold.copy(alpha = 0.45f))
                )
            }
            Text(
                text = "Empowering Kenya's Informal Workers",
                fontSize = 11.sp,
                color = white.copy(alpha = 0.38f),
                letterSpacing = 0.8.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
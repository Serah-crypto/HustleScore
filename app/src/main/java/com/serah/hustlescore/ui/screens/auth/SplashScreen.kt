package com.serah.hustlescore.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(navController: NavController) {

    val logoScale    = remember { Animatable(0f) }
    val logoAlpha    = remember { Animatable(0f) }
    val textAlpha    = remember { Animatable(0f) }
    val subtitleAlpha= remember { Animatable(0f) }
    val dotsAlpha    = remember { Animatable(0f) }

    // Pulsing glow ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseScale"
    )

    LaunchedEffect(Unit) {
        // Logo bounces in
        launch { logoAlpha.animateTo(1f, animationSpec = tween(300)) }
        logoScale.animateTo(
            1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
        )
        // Staggered text
        textAlpha.animateTo(1f, animationSpec = tween(500))
        delay(150)
        subtitleAlpha.animateTo(1f, animationSpec = tween(500))
        delay(200)
        dotsAlpha.animateTo(1f, animationSpec = tween(400))

        delay(1800)
        navController.navigate("dashboard") {
            popUpTo("splash_user") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF16A34A), Color(0xFF15803D), Color(0xFF14532D))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative blobs
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(x = 100.dp, y = (-150).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = (-80).dp, y = 180.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = 120.dp, y = 200.dp)
                .clip(CircleShape)
                .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pulsing glow ring behind logo
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                )
                Box(
                    modifier = Modifier
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .size(80.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "H",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF16A34A)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "HustleScore",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Your Money.\nYour Score.\nYour Future.",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )

            Spacer(Modifier.height(32.dp))

            // Animated loading dots
            Row(
                modifier = Modifier.alpha(dotsAlpha.value),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(0, 150, 300).forEach { delayMs ->
                    val dotScale by rememberInfiniteTransition(label = "dot$delayMs")
                        .animateFloat(
                            initialValue = 0.6f, targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(500, delayMillis = delayMs),
                                repeatMode = RepeatMode.Reverse
                            ), label = "dot"
                        )
                    Box(
                        modifier = Modifier
                            .scale(dotScale)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.7f))
                    )
                }
            }
        }

        // Bottom tagline
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .alpha(dotsAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Empowering Kenya's Informal Workers",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
package com.serah.hustlescore.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Data model
// ─────────────────────────────────────────────────────────────────────────────
private data class OnboardPage(
    val index: Int,
    val eyebrow: String,
    val headline: String,
    val accent: String,        // coloured word appended after headline
    val body: String,
    val stat: String,          // big oversized stat
    val statLabel: String,
    val illustrationKey: Int   // drives which illustration composable to draw
)

private val pages = listOf(
    OnboardPage(
        index = 0,
        eyebrow = "STEP 01 OF 03",
        headline = "Your M-Pesa tells\na powerful",
        accent = "story.",
        body = "We read your M-Pesa statement — securely, privately — to understand your real income patterns and spending habits.",
        stat = "2M+",
        statLabel = "informal workers\nalready using M-Pesa",
        illustrationKey = 0
    ),
    OnboardPage(
        index = 1,
        eyebrow = "STEP 02 OF 03",
        headline = "Turn transactions\ninto a credit",
        accent = "score.",
        body = "HustleScore converts your everyday transactions into a verified financial identity — recognised by lenders across Kenya.",
        stat = "850",
        statLabel = "max score you\ncan achieve",
        illustrationKey = 1
    ),
    OnboardPage(
        index = 2,
        eyebrow = "STEP 03 OF 03",
        headline = "Unlock loans,\nsavings & your",
        accent = "future.",
        body = "With your HustleScore you can apply for microloans, savings products, and insurance — all built for Kenya's hustlers.",
        stat = "0%",
        statLabel = "data sold\nto third parties",
        illustrationKey = 2
    )
)

// ─────────────────────────────────────────────────────────────────────────────
// Colour palette (matches SplashScreen)
// ─────────────────────────────────────────────────────────────────────────────
private val DeepGreen     = Color(0xFF011710)
private val MidGreen      = Color(0xFF022C18)
private val BrightGreen   = Color(0xFF16A34A)
private val LimeGreen     = Color(0xFF4ADE80)
private val EmeraldAccent = Color(0xFF34D399)
private val Gold          = Color(0xFFFBBF24)
private val GoldLight     = Color(0xFFFDE68A)
private val White         = Color.White

// ─────────────────────────────────────────────────────────────────────────────
// Main composable
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun OnboardingScreen(navController: NavController) {

    var currentPage by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    // Slide-level animation drivers
    val contentAlpha = remember { Animatable(1f) }
    val contentSlide = remember { Animatable(0f) }

    // Shared infinite animations
    val infinite = rememberInfiniteTransition(label = "bg")
    val float1 by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing)),
        label = "orb1"
    )
    val float2 by infinite.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(24000, easing = LinearEasing)),
        label = "orb2"
    )
    val breathe by infinite.animateFloat(
        initialValue = 0.96f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(2600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "breathe"
    )
    val shimmer by infinite.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )

    fun navigateTo(index: Int) {
        if (index == pages.size) {
            navController.navigate("login") {
                popUpTo("onboarding") { inclusive = true }
            }
            return
        }
        scope.launch {
            // Fade + slide out
            launch { contentAlpha.animateTo(0f, tween(200)) }
            contentSlide.animateTo((-30).toFloat(), tween(200, easing = EaseInCubic))
            currentPage = index
            // Reset slide from right then animate in
            contentSlide.snapTo(40f)
            launch { contentAlpha.animateTo(1f, tween(320)) }
            contentSlide.animateTo(0f, tween(320, easing = EaseOutCubic))
        }
    }

    val page = pages[currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to DeepGreen,
                    0.45f to MidGreen,
                    0.80f to Color(0xFF034D25),
                    1f to Color(0xFF022820)
                )
            )
            .pointerInput(currentPage) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -40 && currentPage < pages.size - 1) navigateTo(currentPage + 1)
                    else if (dragAmount > 40 && currentPage > 0) navigateTo(currentPage - 1)
                }
            }
    ) {

        // ── Animated orbital background blobs ─────────────────────────────
        Box(
            modifier = Modifier
                .size(420.dp)
                .align(Alignment.TopEnd)
                .offset(x = 120.dp, y = (-90).dp)
                .rotate(float1)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(BrightGreen.copy(alpha = 0.13f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 80.dp)
                .rotate(float2)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Gold.copy(alpha = 0.10f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 60.dp, y = 40.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(LimeGreen.copy(alpha = 0.06f), Color.Transparent)
                    )
                )
        )

        // ── Diagonal accent stripe ────────────────────────────────────────
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(200.dp)
                .align(Alignment.TopStart)
                .offset(x = 28.dp, y = 100.dp)
                .rotate(-15f)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Gold.copy(alpha = 0.20f), Color.Transparent)
                    )
                )
        )

        // ── Main content column ───────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .offset(y = contentSlide.value.dp)
                .alpha(contentAlpha.value)
        ) {

            Spacer(Modifier.height(56.dp))

            // ── Top row: eyebrow + skip ───────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Eyebrow pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(White.copy(alpha = 0.08f))
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(Gold.copy(alpha = 0.40f), EmeraldAccent.copy(alpha = 0.20f))
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = page.eyebrow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gold.copy(alpha = 0.90f),
                        letterSpacing = 1.2.sp
                    )
                }

                // Skip button
                Text(
                    text = "Skip",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = White.copy(alpha = 0.38f),
                    letterSpacing = 0.4.sp,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { navigateTo(pages.size) }
                        .padding(8.dp)
                )
            }

            Spacer(Modifier.height(36.dp))

            // ── Illustration card ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .scale(breathe)
                    .shadow(
                        elevation = 32.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = LimeGreen.copy(alpha = 0.15f),
                        spotColor = BrightGreen.copy(alpha = 0.20f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF023D20), Color(0xFF011F12))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                EmeraldAccent.copy(alpha = 0.30f),
                                Color.Transparent,
                                Gold.copy(alpha = 0.15f)
                            )
                        ),
                        RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Per-slide illustration
                when (page.illustrationKey) {
                    0 -> MpesaIllustration(shimmer, breathe)
                    1 -> ScoreIllustration(shimmer, breathe, infinite)
                    2 -> FutureIllustration(shimmer, breathe)
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Stat badge ────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Stat number
                Text(
                    text = page.stat,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-2).sp,
                    color = Gold,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Gold.copy(alpha = 0.40f),
                            blurRadius = 24f
                        )
                    )
                )
                // Vertical divider
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(44.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Gold.copy(alpha = 0.50f), Color.Transparent)
                            )
                        )
                )
                Text(
                    text = page.statLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = White.copy(alpha = 0.55f),
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Headline ──────────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = page.headline,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = White,
                        lineHeight = 36.sp,
                        letterSpacing = (-0.8).sp
                    )
                    Text(
                        text = page.accent,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.8).sp,
                        color = LimeGreen,
                        style = TextStyle(
                            shadow = Shadow(
                                color = LimeGreen.copy(alpha = 0.50f),
                                blurRadius = 16f
                            )
                        )
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Body ──────────────────────────────────────────────────────
            Text(
                text = page.body,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = White.copy(alpha = 0.62f),
                lineHeight = 22.sp,
                letterSpacing = 0.1.sp
            )

            Spacer(Modifier.weight(1f))

            // ── Bottom row: dots + CTA ────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 44.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress dots
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    pages.forEachIndexed { i, _ ->
                        val active = i == currentPage
                        val dotWidth by animateDpAsState(
                            targetValue = if (active) 28.dp else 7.dp,
                            animationSpec = tween(300, easing = EaseOutCubic),
                            label = "dotW$i"
                        )
                        val dotAlpha by animateFloatAsState(
                            targetValue = if (active) 1f else 0.35f,
                            animationSpec = tween(300), label = "dotA$i"
                        )
                        Box(
                            modifier = Modifier
                                .width(dotWidth)
                                .height(7.dp)
                                .alpha(dotAlpha)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (active)
                                        Brush.horizontalGradient(listOf(LimeGreen, Gold))
                                    else
                                        Brush.horizontalGradient(
                                            listOf(White.copy(0.30f), White.copy(0.30f))
                                        )
                                )
                        )
                    }
                }

                // CTA button
                val isLast = currentPage == pages.size - 1
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(50.dp),
                            ambientColor = if (isLast) Gold.copy(0.40f) else LimeGreen.copy(0.30f),
                            spotColor = if (isLast) Gold.copy(0.50f) else LimeGreen.copy(0.40f)
                        )
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            if (isLast)
                                Brush.linearGradient(listOf(Gold, Color(0xFFF59E0B)))
                            else
                                Brush.linearGradient(listOf(BrightGreen, Color(0xFF15803D)))
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { navigateTo(currentPage + 1) }
                        .padding(horizontal = 28.dp, vertical = 15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLast) "Get Started →" else "Next →",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLast) Color(0xFF1A0F00) else White,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Slide 1 — M-Pesa illustration
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MpesaIllustration(shimmer: Float, breathe: Float) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        // Background glow
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(BrightGreen.copy(0.20f), Color.Transparent)))
        )

        // Phone frame
        Box(
            modifier = Modifier
                .width(90.dp)
                .height(155.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF023B1E))
                .border(1.5.dp, EmeraldAccent.copy(0.50f), RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Spacer(Modifier.height(6.dp))
                // M-Pesa header bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(BrightGreen.copy(0.70f))
                )
                // Transaction rows
                repeat(4) { i ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(White.copy(0.18f))
                        )
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (i % 2 == 0) LimeGreen.copy(0.60f) else Gold.copy(0.50f))
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                // Amount display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                0f to Color.Transparent,
                                shimmer.coerceIn(0f, 1f) to GoldLight.copy(0.25f),
                                1f to Color.Transparent
                            )
                        )
                        .border(1.dp, Gold.copy(0.30f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("KES 42,800", fontSize = 9.sp, color = Gold, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Floating transaction chips
        Box(
            modifier = Modifier
                .offset(x = 70.dp, y = (-45).dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF023B1E))
                .border(1.dp, LimeGreen.copy(0.35f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text("↑ KES 5,200", fontSize = 9.sp, color = LimeGreen, fontWeight = FontWeight.SemiBold)
        }
        Box(
            modifier = Modifier
                .offset(x = (-72).dp, y = 40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF023B1E))
                .border(1.dp, Gold.copy(0.35f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text("↓ KES 1,800", fontSize = 9.sp, color = Gold, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Slide 2 — Credit score dial illustration
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ScoreIllustration(
    shimmer: Float,
    breathe: Float,
    infinite: InfiniteTransition
) {
    val arcSweep by infinite.animateFloat(
        initialValue = 60f, targetValue = 195f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "arc"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        // Outer glow ring
        Box(
            modifier = Modifier
                .size(170.dp)
                .scale(breathe)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(LimeGreen.copy(0.12f), Color.Transparent)))
        )

        // Score dial card
        Box(
            modifier = Modifier
                .size(140.dp)
                .shadow(20.dp, CircleShape, ambientColor = Gold.copy(0.30f), spotColor = Gold.copy(0.30f))
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF034D25), Color(0xFF011F12))))
                .border(
                    2.dp,
                    Brush.sweepGradient(
                        listOf(
                            LimeGreen.copy(0f),
                            LimeGreen.copy(0.70f),
                            Gold,
                            Gold.copy(0.60f),
                            LimeGreen.copy(0f)
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "720",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = Gold,
                    letterSpacing = (-1.5).sp,
                    style = TextStyle(shadow = Shadow(Gold.copy(0.45f), blurRadius = 20f))
                )
                Text(
                    text = "GOOD",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = LimeGreen,
                    letterSpacing = 2.sp
                )
            }
        }

        // Floating label chips
        Box(
            modifier = Modifier
                .offset(x = 78.dp, y = 30.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF023B1E))
                .border(1.dp, EmeraldAccent.copy(0.40f), RoundedCornerShape(10.dp))
                .padding(horizontal = 9.dp, vertical = 5.dp)
        ) {
            Text("↑ +45 pts", fontSize = 9.sp, color = LimeGreen, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier
                .offset(x = (-76).dp, y = (-36).dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF023B1E))
                .border(1.dp, Gold.copy(0.35f), RoundedCornerShape(10.dp))
                .padding(horizontal = 9.dp, vertical = 5.dp)
        ) {
            Text("FAIR → GOOD", fontSize = 9.sp, color = Gold, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Slide 3 — Future / unlock illustration
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FutureIllustration(shimmer: Float, breathe: Float) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Gold.copy(0.13f), Color.Transparent)))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Three unlock product cards
            listOf(
                Triple("🏦", "Microloan", "Up to KES 50,000"),
                Triple("💰", "Savings", "Earn 12% p.a."),
                Triple("🛡️", "Insurance", "From KES 200/mo")
            ).forEachIndexed { i, (icon, label, sub) ->
                val offsetX = listOf(0, 20, 0)[i].dp
                Box(
                    modifier = Modifier
                        .offset(x = offsetX)
                        .width(200.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF034D25).copy(0.80f), Color(0xFF023B1E).copy(0.80f))
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    if (i == 0) Gold.copy(0.50f) else EmeraldAccent.copy(0.30f),
                                    Color.Transparent
                                )
                            ),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(icon, fontSize = 20.sp)
                        Column {
                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = White)
                            Text(sub, fontSize = 10.sp, color = White.copy(0.55f))
                        }
                        Spacer(Modifier.weight(1f))
                        // Unlock indicator
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i == 0) Brush.radialGradient(listOf(Gold, Color(0xFFF59E0B)))
                                    else Brush.radialGradient(listOf(EmeraldAccent.copy(0.40f), Color.Transparent))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (i == 0) "✓" else "→",
                                fontSize = 9.sp,
                                color = if (i == 0) Color(0xFF1A0F00) else White.copy(0.60f),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
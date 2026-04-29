package com.serah.hustlescore.components


import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serah.hustlescore.data.algorithm.HustleScoreEngine

@Composable
fun ScoreGauge(
    score: Int,
    size: Dp = 160.dp,
    strokeWidth: Float = 18f
) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "score"
    )

    val sweepAngle by animateFloatAsState(
        targetValue = (score / 1000f) * 360f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "sweep"
    )



    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = this.size
            val arcSize = Size(canvasSize.width - strokeWidth, canvasSize.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Background arc
            drawArc(color = Color.White.copy(alpha = 0.2f), startAngle = -90f, sweepAngle = 360f,
                useCenter = false, topLeft = topLeft, size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

            // Foreground arc
            drawArc(color = Color.White, startAngle = -90f, sweepAngle = sweepAngle,
                useCenter = false, topLeft = topLeft, size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$animatedScore", color = Color.White, fontSize = (size.value * 0.22).sp, fontWeight = FontWeight.Black)
            Text(text = "/ 1000", color = Color.White.copy(alpha = 0.7f), fontSize = (size.value * 0.09).sp)
        }
    }
}
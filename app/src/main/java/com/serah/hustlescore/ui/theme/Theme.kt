package com.hustlescore.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.serah.hustlescore.ui.theme.BorderColor
import com.serah.hustlescore.ui.theme.CardWhite
import com.serah.hustlescore.ui.theme.HustleDark
import com.serah.hustlescore.ui.theme.HustleGold
import com.serah.hustlescore.ui.theme.HustleGreen
import com.serah.hustlescore.ui.theme.HustleSurface
import com.serah.hustlescore.ui.theme.ScoreExcellent
import com.serah.hustlescore.ui.theme.ScoreFair
import com.serah.hustlescore.ui.theme.ScoreGood
import com.serah.hustlescore.ui.theme.ScorePoor
import com.serah.hustlescore.ui.theme.TextPrimary

val HustleTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 28.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp)
)

private val LightColorScheme = lightColorScheme(
    primary = HustleGreen,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1FAE5),
    secondary = HustleGold,
    onSecondary = HustleDark,
    background = HustleSurface,
    surface = CardWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = BorderColor
)


fun getScoreColor(score: Int): Color = when {
    score >= 750 -> ScoreExcellent
    score >= 550 -> ScoreGood
    score >= 350 -> ScoreFair
    else         -> ScorePoor
}
@Composable
fun HustleScoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // This should now come from your ViewModel
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme // Define your dark colors here
    } else {
        LightColorScheme // Define your light colors here
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

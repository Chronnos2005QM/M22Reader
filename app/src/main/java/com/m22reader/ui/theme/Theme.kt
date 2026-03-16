package com.m22reader.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Brand colours ────────────────────────────────────────────────────────────
val M22Purple       = Color(0xFF9333EA)
val M22Pink         = Color(0xFFDB2777)
val M22PurpleDark   = Color(0xFF7C3AED)
val M22PurpleLight  = Color(0xFFE9D5FF)

// Dark palette
val DarkBackground  = Color(0xFF0D0D12)
val DarkSurface     = Color(0xFF13131A)
val DarkSurfaceAlt  = Color(0xFF1A1A24)
val DarkBorder      = Color(0xFF1E1E2A)
val DarkTextPrimary = Color(0xFFE8E6F0)
val DarkTextMuted   = Color(0xFF6B6880)

// Light palette
val LightBackground = Color(0xFFF4F3F8)
val LightSurface    = Color(0xFFFFFFFF)
val LightSurfaceAlt = Color(0xFFF0EEF8)
val LightTextPrimary= Color(0xFF1A1824)
val LightTextMuted  = Color(0xFF8A85A0)

// ── Colour schemes ────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary          = M22Purple,
    secondary        = M22Pink,
    background       = DarkBackground,
    surface          = DarkSurface,
    surfaceVariant   = DarkSurfaceAlt,
    onPrimary        = Color.White,
    onBackground     = DarkTextPrimary,
    onSurface        = DarkTextPrimary,
    outline          = DarkBorder,
)

private val LightColorScheme = lightColorScheme(
    primary          = M22PurpleDark,
    secondary        = M22Pink,
    background       = LightBackground,
    surface          = LightSurface,
    surfaceVariant   = LightSurfaceAlt,
    onPrimary        = Color.White,
    onBackground     = LightTextPrimary,
    onSurface        = LightTextPrimary,
    outline          = Color(0xFFE0DCF0),
)

// ── Typography ────────────────────────────────────────────────────────────────
val M22Typography = Typography(
    headlineLarge  = TextStyle(fontWeight = FontWeight.Black,  fontSize = 28.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 22.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 18.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall      = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelSmall     = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 10.sp, letterSpacing = 1.sp),
)

// ── Theme composable ──────────────────────────────────────────────────────────
@Composable
fun M22ReaderTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = M22Typography,
        content     = content
    )
}

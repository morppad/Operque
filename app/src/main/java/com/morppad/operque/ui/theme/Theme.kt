package com.morppad.operque.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val JiraBlue = Color(0xFF0C66E4)
val JiraBlueDark = Color(0xFF0052CC)
val JiraBlueLight = Color(0xFFE9F2FF)
val JiraSurface = Color(0xFFFFFFFF)
val JiraBackground = Color(0xFFF7F8F9)
val JiraBorder = Color(0xFFDCDFE4)
val JiraText = Color(0xFF172B4D)
val JiraTextSubtle = Color(0xFF626F86)
val JiraGreen = Color(0xFF1F845A)
val JiraGreenLight = Color(0xFFDCFFF1)
val JiraYellow = Color(0xFF946F00)
val JiraYellowLight = Color(0xFFFFF7D6)
val JiraRed = Color(0xFFCA3521)
val JiraRedLight = Color(0xFFFFEDEB)

private val JiraColors = lightColorScheme(
    primary = JiraBlue,
    onPrimary = Color.White,
    primaryContainer = JiraBlueLight,
    onPrimaryContainer = JiraBlueDark,
    secondary = Color(0xFF579DFF),
    onSecondary = JiraText,
    secondaryContainer = JiraBlueLight,
    onSecondaryContainer = JiraBlueDark,
    tertiary = JiraBlueDark,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF1F2F4),
    onTertiaryContainer = JiraText,
    background = JiraBackground,
    onBackground = JiraText,
    surface = JiraSurface,
    onSurface = JiraText,
    surfaceVariant = Color(0xFFF1F2F4),
    onSurfaceVariant = JiraTextSubtle,
    outline = JiraBorder,
    error = JiraRed,
    errorContainer = JiraRedLight,
    onErrorContainer = JiraRed
)

private val JiraTypography = Typography(
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
)

private val JiraShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp)
)

@Composable
fun OperqueTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = JiraColors,
        typography = JiraTypography,
        shapes = JiraShapes,
        content = content
    )
}

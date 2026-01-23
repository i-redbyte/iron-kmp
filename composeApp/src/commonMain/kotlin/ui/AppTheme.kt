package ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HackerScheme = darkColorScheme(
    background = Color(0xFF050607),
    surface = Color(0xFF0B0F10),
    primary = Color(0xFF0DD00D),
    secondary = Color(0xFF00D9FF),
    tertiary = Color(0xFF9D4BFF),
    onBackground = Color(0xFFE6FFF2),
    onSurface = Color(0xFFE6FFF2),
    onPrimary = Color(0xFF00140A)
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HackerScheme,
        typography = Typography(),
        content = content
    )
}

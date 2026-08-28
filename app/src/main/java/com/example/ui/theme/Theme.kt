package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val ClassicBlueScheme = lightColorScheme(
    primary = Color(0xFF185ABD),
    secondary = Color(0xFF2B579A),
    tertiary = Color(0xFF4688F1),
    background = Color(0xFFF9F9F9),
    surface = Color(0xFFF9F9F9),
    onPrimary = Color.White,
    onBackground = Color(0xFF202124),
    onSurface = Color(0xFF202124)
)

private val EmeraldGreenScheme = lightColorScheme(
    primary = Color(0xFF00796B),
    secondary = Color(0xFF004D40),
    tertiary = Color(0xFF4DB6AC),
    background = Color(0xFFF1F8E9),
    surface = Color(0xFFF1F8E9),
    onPrimary = Color.White,
    onBackground = Color(0xFF1B5E20),
    onSurface = Color(0xFF1B5E20)
)

private val MidnightPurpleScheme = lightColorScheme(
    primary = Color(0xFF512DA8),
    secondary = Color(0xFF311B92),
    tertiary = Color(0xFF7E57C2),
    background = Color(0xFFF3E5F5),
    surface = Color(0xFFF3E5F5),
    onPrimary = Color.White,
    onBackground = Color(0xFF311B92),
    onSurface = Color(0xFF311B92)
)

private val RubyRedScheme = lightColorScheme(
    primary = Color(0xFFC62828),
    secondary = Color(0xFFB71C1C),
    tertiary = Color(0xFFEF5350),
    background = Color(0xFFFFEBEE),
    surface = Color(0xFFFFEBEE),
    onPrimary = Color.White,
    onBackground = Color(0xFFB71C1C),
    onSurface = Color(0xFFB71C1C)
)

@Composable
fun WordEditorTheme(
    content: @Composable () -> Unit
) {
    val currentTheme by ThemeManager.currentTheme.collectAsState()
    
    val colorScheme = when (currentTheme) {
        AppThemeOption.CLASSIC_BLUE -> ClassicBlueScheme
        AppThemeOption.EMERALD_GREEN -> EmeraldGreenScheme
        AppThemeOption.MIDNIGHT_PURPLE -> MidnightPurpleScheme
        AppThemeOption.RUBY_RED -> RubyRedScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeOption(val title: String) {
    CLASSIC_BLUE("الكلاسيكي الأزرق (افتراضي)"),
    EMERALD_GREEN("الزمرد الأخضر"),
    MIDNIGHT_PURPLE("البنفسجي الليلي"),
    RUBY_RED("الياقوت الأحمر")
}

object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val THEME_KEY = "selected_theme"

    private val _currentTheme = MutableStateFlow(AppThemeOption.CLASSIC_BLUE)
    val currentTheme: StateFlow<AppThemeOption> = _currentTheme.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedThemeName = prefs.getString(THEME_KEY, AppThemeOption.CLASSIC_BLUE.name)
        val savedTheme = try {
            AppThemeOption.valueOf(savedThemeName ?: AppThemeOption.CLASSIC_BLUE.name)
        } catch (e: Exception) {
            AppThemeOption.CLASSIC_BLUE
        }
        _currentTheme.value = savedTheme
    }

    fun setTheme(context: Context, theme: AppThemeOption) {
        _currentTheme.value = theme
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(THEME_KEY, theme.name).apply()
    }
}

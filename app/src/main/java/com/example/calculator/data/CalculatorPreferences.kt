package com.example.calculator.data

import android.content.Context
import android.content.SharedPreferences
import com.example.calculator.engine.AngleMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

data class UserSettings(
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val angleMode: AngleMode = AngleMode.DEGREE,
    val decimalPrecision: Int = -1, // -1 means Auto
    val useGrouping: Boolean = true,
    val hapticFeedback: Boolean = true
)

class CalculatorPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("waqar_calc_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val themeOrdinal = prefs.getInt(KEY_THEME, AppThemeMode.DARK.ordinal)
        val angleOrdinal = prefs.getInt(KEY_ANGLE, AngleMode.DEGREE.ordinal)
        val precision = prefs.getInt(KEY_PRECISION, -1)
        val grouping = prefs.getBoolean(KEY_GROUPING, true)
        val haptic = prefs.getBoolean(KEY_HAPTIC, true)

        val theme = AppThemeMode.entries.getOrElse(themeOrdinal) { AppThemeMode.DARK }
        val angle = AngleMode.entries.getOrElse(angleOrdinal) { AngleMode.DEGREE }

        return UserSettings(
            themeMode = theme,
            angleMode = angle,
            decimalPrecision = precision,
            useGrouping = grouping,
            hapticFeedback = haptic
        )
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putInt(KEY_THEME, mode.ordinal).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun setAngleMode(mode: AngleMode) {
        prefs.edit().putInt(KEY_ANGLE, mode.ordinal).apply()
        _settings.value = _settings.value.copy(angleMode = mode)
    }

    fun setDecimalPrecision(precision: Int) {
        prefs.edit().putInt(KEY_PRECISION, precision).apply()
        _settings.value = _settings.value.copy(decimalPrecision = precision)
    }

    fun setUseGrouping(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GROUPING, enabled).apply()
        _settings.value = _settings.value.copy(useGrouping = enabled)
    }

    fun setHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        _settings.value = _settings.value.copy(hapticFeedback = enabled)
    }

    companion object {
        private const val KEY_THEME = "theme_mode"
        private const val KEY_ANGLE = "angle_mode"
        private const val KEY_PRECISION = "decimal_precision"
        private const val KEY_GROUPING = "use_grouping"
        private const val KEY_HAPTIC = "haptic_feedback"
    }
}

package com.example.classapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),   // Light Blue
    secondary = Color(0xFF81C784), // Light Green
    tertiary = Color(0xFFFFF176)   // Light Yellow
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),   // Blue
    secondary = Color(0xFF388E3C), // Green
    tertiary = Color(0xFFFFA000)   // Orange
)

@Composable
fun ClassappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
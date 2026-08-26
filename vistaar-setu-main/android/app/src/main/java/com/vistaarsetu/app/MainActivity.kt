package com.vistaarsetu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.vistaarsetu.app.ui.MainAppNavigation

private val VistaarLightColorScheme = lightColorScheme(
    primary = Color(0xFF7C3AED),             // Vivid Purple Accent
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),    // Light Lavender Container
    onPrimaryContainer = Color(0xFF5B21B6),  // Dark Purple Text
    secondary = Color(0xFF8B5CF6),           // Secondary Light Purple
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = Color(0xFF6B21A8),
    tertiary = Color(0xFF9333EA),
    onTertiary = Color.White,
    background = Color(0xFFF6F5FB),         // Light Lavender Background
    onBackground = Color(0xFF1E1B4B),       // Dark Indigo Text
    surface = Color(0xFFFFFFFF),            // Clean White Card Surface
    onSurface = Color(0xFF1E1B4B),
    surfaceVariant = Color(0xFFF3F0FF),
    onSurfaceVariant = Color(0xFF6B7280)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = VistaarLightColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppNavigation()
                }
            }
        }
    }
}
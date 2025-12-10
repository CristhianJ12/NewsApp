// ==========================================
// 📄 ARCHIVO: Theme.kt
// 📁 UBICACIÓN: presentation/ui/theme/
// 🎨 TIPO: Composable (Theme)
// ==========================================

package com.example.newsapp.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Esquema de colores oscuros
 */
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF004A77),
    onPrimaryContainer = Color(0xFFD1E4FF),

    secondary = SecondaryLight,
    onSecondary = Color(0xFF003730),
    secondaryContainer = Color(0xFF005047),
    onSecondaryContainer = Color(0xFFA7F2E6),

    tertiary = TertiaryLight,
    onTertiary = Color(0xFF4D2600),
    tertiaryContainer = Color(0xFF6E3900),
    onTertiaryContainer = Color(0xFFFFDCC1),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = BackgroundDark,
    onBackground = Color(0xFFE1E2EC),

    surface = SurfaceDark,
    onSurface = Color(0xFFE1E2EC),
    surfaceVariant = Color(0xFF42474E),
    onSurfaceVariant = Color(0xFFC2C7CF),

    outline = Color(0xFF8C9199),
    outlineVariant = Color(0xFF42474E)
)

/**
 * Esquema de colores claros
 */
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),

    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA7F2E6),
    onSecondaryContainer = Color(0xFF00201B),

    tertiary = Tertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDCC1),
    onTertiaryContainer = Color(0xFF291800),

    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = BackgroundLight,
    onBackground = Color(0xFF1A1C1E),

    surface = SurfaceLight,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF42474E),

    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC2C7CF)
)

/**
 * Tema principal de la aplicación
 *
 * @param darkTheme Si debe usar tema oscuro (por defecto usa el del sistema)
 * @param dynamicColor Si debe usar colores dinámicos de Android 12+ (Material You)
 * @param content Contenido de la app
 */
@Composable
fun NewsAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Habilita Material You en Android 12+
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Material You (Android 12+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        // Tema oscuro tradicional
        darkTheme -> DarkColorScheme
        // Tema claro tradicional
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// ==========================================
// FIN DE ARCHIVO Theme.kt
// ==========================================
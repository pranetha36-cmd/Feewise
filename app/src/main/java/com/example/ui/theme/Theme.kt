package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Navy100,
    onPrimary = Navy950,
    primaryContainer = Navy800,
    onPrimaryContainer = Navy100,
    secondary = GreenProgress,
    onSecondary = PureWhite,
    secondaryContainer = GreenProgressDark,
    onSecondaryContainer = GreenLight,
    tertiary = OrangeDeadline,
    onTertiary = PureWhite,
    background = Navy950,
    surface = Navy900,
    onBackground = SlateBackground,
    onSurface = SlateBackground,
    error = RedOverdue,
    onError = PureWhite
)

private val LightColorScheme = lightColorScheme(
    primary = Navy900,
    onPrimary = PureWhite,
    primaryContainer = Navy50,
    onPrimaryContainer = Navy900,
    secondary = GreenProgress,
    onSecondary = PureWhite,
    secondaryContainer = GreenLight,
    onSecondaryContainer = GreenProgressDark,
    tertiary = OrangeDeadline,
    onTertiary = PureWhite,
    tertiaryContainer = OrangeLight,
    onTertiaryContainer = OrangeDeadlineDark,
    background = SlateBackground,
    surface = CardSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = Navy50,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight,
    outlineVariant = DividerColor,
    error = RedOverdue,
    onError = PureWhite,
    errorContainer = RedLight,
    onErrorContainer = RedOverdueDark
)

@Composable
fun FeeWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent branding colors by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

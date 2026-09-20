package com.example.ui.theme

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

// Clean Light Theme by default
private val ModernLightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = PrimaryGreenContainer,
    onPrimaryContainer = OnPrimaryGreenContainer,
    secondary = BlueBorrow,
    onSecondary = Color.White,
    secondaryContainer = BlueBorrowContainer,
    onSecondaryContainer = OnBlueBorrowContainer,
    tertiary = PurpleSettled,
    onTertiary = Color.White,
    tertiaryContainer = PurpleSettledContainer,
    onTertiaryContainer = OnPurpleSettledContainer,
    error = RedOverdue,
    onError = Color.White,
    errorContainer = RedOverdueContainer,
    onErrorContainer = OnRedOverdueContainer,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    outline = OutlineLight,
    outlineVariant = OutlineSubtle
)

private val ModernDarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C995),
    onPrimary = Color(0xFF00391A),
    primaryContainer = Color(0xFF0F522E),
    onPrimaryContainer = Color(0xFFA8E6BC),
    secondary = Color(0xFF8AB4F8),
    onSecondary = Color(0xFF003062),
    secondaryContainer = Color(0xFF004990),
    onSecondaryContainer = Color(0xFFD2E3FC),
    background = Color(0xFF131316),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF1E1F24),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF2B2C33),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF44474E)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default Light Theme as explicitly requested
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ModernDarkColorScheme else ModernLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

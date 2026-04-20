package me.rerere.rikkahub.ui.theme.presets

import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import me.rerere.rikkahub.ui.theme.PresetTheme

val NightingaleThemePreset by lazy {
    PresetTheme(
        id = "nightingale",
        name = {
            Text("Nightingale")
        },
        standardLight = nightingaleLightScheme,
        standardDark = nightingaleDarkScheme,
    )
}

// Nightingale Theme — deep navy + gold accents
// Base: #101018 (background), #091931 (surface), #07223F (primary), #D8CAAD (gold accent)

// Dark scheme (primary use case)
private val primaryDark = Color(0xFFD8CAAD)           // gold — buttons, accents
private val onPrimaryDark = Color(0xFF07223F)          // navy text on gold
private val primaryContainerDark = Color(0xFF0D3158)   // deeper navy container
private val onPrimaryContainerDark = Color(0xFFE8DCC6) // light gold on container
private val secondaryDark = Color(0xFFB8C4D4)          // muted blue-silver
private val onSecondaryDark = Color(0xFF0A1929)        // deep navy
private val secondaryContainerDark = Color(0xFF1A3049)  // medium navy
private val onSecondaryContainerDark = Color(0xFFD4DFEE) // pale blue
private val tertiaryDark = Color(0xFFCBB98A)           // warm gold variant
private val onTertiaryDark = Color(0xFF2E2008)         // dark brown
private val tertiaryContainerDark = Color(0xFF463313)   // warm brown container
private val onTertiaryContainerDark = Color(0xFFE8D5A4) // light warm gold
private val errorDark = Color(0xFFFFB4AB)
private val onErrorDark = Color(0xFF690005)
private val errorContainerDark = Color(0xFF93000A)
private val onErrorContainerDark = Color(0xFFFFDAD6)
private val backgroundDark = Color(0xFF101018)          // base background
private val onBackgroundDark = Color(0xFFE2DDE6)        // light text
private val surfaceDark = Color(0xFF101018)             // same as background
private val onSurfaceDark = Color(0xFFE2DDE6)           // light text
private val surfaceVariantDark = Color(0xFF2A3545)      // slightly lighter navy
private val onSurfaceVariantDark = Color(0xFFC4CCD8)    // muted text
private val outlineDark = Color(0xFF4A5568)             // subtle border
private val outlineVariantDark = Color(0xFF2D3748)      // fainter border
private val scrimDark = Color(0xFF000000)
private val inverseSurfaceDark = Color(0xFFE2DDE6)
private val inverseOnSurfaceDark = Color(0xFF1A1B23)
private val inversePrimaryDark = Color(0xFF07223F)
private val surfaceDimDark = Color(0xFF0D0D14)          // darker than background
private val surfaceBrightDark = Color(0xFF1A1A24)       // slightly brighter
private val surfaceContainerLowestDark = Color(0xFF0A0A10) // deepest
private val surfaceContainerLowDark = Color(0xFF091931)    // navy surface
private val surfaceContainerDark = Color(0xFF0F1E35)       // between navy tones
private val surfaceContainerHighDark = Color(0xFF152740)   // container highlight
private val surfaceContainerHighestDark = Color(0xFF1C304B) // lightest container

// Light scheme (for completeness)
private val primaryLight = Color(0xFF07223F)
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFFD4E3F5)
private val onPrimaryContainerLight = Color(0xFF051A30)
private val secondaryLight = Color(0xFF4A5F78)
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFD0E0F2)
private val onSecondaryContainerLight = Color(0xFF354A62)
private val tertiaryLight = Color(0xFF6B5529)
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFF5E4C1)
private val onTertiaryContainerLight = Color(0xFF523F16)
private val errorLight = Color(0xFFBA1A1A)
private val onErrorLight = Color(0xFFFFFFFF)
private val errorContainerLight = Color(0xFFFFDAD6)
private val onErrorContainerLight = Color(0xFF93000A)
private val backgroundLight = Color(0xFFF8F9FC)
private val onBackgroundLight = Color(0xFF1A1C20)
private val surfaceLight = Color(0xFFF8F9FC)
private val onSurfaceLight = Color(0xFF1A1C20)
private val surfaceVariantLight = Color(0xFFDFE3EB)
private val onSurfaceVariantLight = Color(0xFF434750)
private val outlineLight = Color(0xFF737880)
private val outlineVariantLight = Color(0xFFC3C7CF)
private val scrimLight = Color(0xFF000000)
private val inverseSurfaceLight = Color(0xFF2F3035)
private val inverseOnSurfaceLight = Color(0xFFF0F0F5)
private val inversePrimaryLight = Color(0xFFD8CAAD)
private val surfaceDimLight = Color(0xFFD8D9DD)
private val surfaceBrightLight = Color(0xFFF8F9FC)
private val surfaceContainerLowestLight = Color(0xFFFFFFFF)
private val surfaceContainerLowLight = Color(0xFFF2F3F6)
private val surfaceContainerLight = Color(0xFFECEDF1)
private val surfaceContainerHighLight = Color(0xFFE6E7EB)
private val surfaceContainerHighestLight = Color(0xFFE1E2E5)

private val nightingaleDarkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

private val nightingaleLightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

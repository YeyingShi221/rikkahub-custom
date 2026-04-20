package me.rerere.rikkahub.ui.theme.presets

import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import me.rerere.rikkahub.ui.theme.PresetTheme

val VerrouilleThemePreset by lazy {
    PresetTheme(
        id = "verrouille",
        name = {
            Text("Verrouillé")
        },
        standardLight = verrouilleLightScheme,
        standardDark = verrouilleDarkScheme,
    )
}

// Verrouillé Theme v2 — muted forest green with warm off-white text
// Palette: #0C1713 (bg), #1E2B18 (main), #152618 (secondary), #6F8266 (button), #474843 (misc), #ECECEB (text)

// Dark scheme
private val primaryDark = Color(0xFF6F8266)             // button — warm sage green
private val onPrimaryDark = Color(0xFF0C1713)           // deep bg on button
private val primaryContainerDark = Color(0xFF1E2B18)    // main container — olive-green
private val onPrimaryContainerDark = Color(0xFFECECEB)  // warm near-white text
private val secondaryDark = Color(0xFF8A9685)           // lighter sage for secondary elements
private val onSecondaryDark = Color(0xFF0F1B15)
private val secondaryContainerDark = Color(0xFF152618)  // darker green container
private val onSecondaryContainerDark = Color(0xFFECECEB)
private val tertiaryDark = Color(0xFF8A8B85)            // misc accent elevated
private val onTertiaryDark = Color(0xFF1A1B18)
private val tertiaryContainerDark = Color(0xFF474843)   // misc accent container
private val onTertiaryContainerDark = Color(0xFFECECEB)
private val errorDark = Color(0xFFFFB4AB)
private val onErrorDark = Color(0xFF690005)
private val errorContainerDark = Color(0xFF93000A)
private val onErrorContainerDark = Color(0xFFFFDAD6)
private val backgroundDark = Color(0xFF0C1713)
private val onBackgroundDark = Color(0xFFECECEB)        // warm off-white text
private val surfaceDark = Color(0xFF0C1713)
private val onSurfaceDark = Color(0xFFECECEB)
private val surfaceVariantDark = Color(0xFF1E2B18)
private val onSurfaceVariantDark = Color(0xFFC4C4C3)    // softer text variant
private val outlineDark = Color(0xFF474843)             // misc accent as border
private val outlineVariantDark = Color(0xFF2A3225)      // fainter border
private val scrimDark = Color(0xFF000000)
private val inverseSurfaceDark = Color(0xFFECECEB)
private val inverseOnSurfaceDark = Color(0xFF1E2B18)
private val inversePrimaryDark = Color(0xFF1E2B18)
private val surfaceDimDark = Color(0xFF080F0C)
private val surfaceBrightDark = Color(0xFF1E2B18)
private val surfaceContainerLowestDark = Color(0xFF060B08)
private val surfaceContainerLowDark = Color(0xFF0F1B14)
private val surfaceContainerDark = Color(0xFF152618)     // secondary main
private val surfaceContainerHighDark = Color(0xFF1B2917)
private val surfaceContainerHighestDark = Color(0xFF1E2B18) // main container

// Light scheme
private val primaryLight = Color(0xFF3A5030)
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFFBDD4AF)
private val onPrimaryContainerLight = Color(0xFF1E2B18)
private val secondaryLight = Color(0xFF4A5A42)
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFC8D8BE)
private val onSecondaryContainerLight = Color(0xFF152618)
private val tertiaryLight = Color(0xFF5A5A52)
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFDDDDD5)
private val onTertiaryContainerLight = Color(0xFF474843)
private val errorLight = Color(0xFFBA1A1A)
private val onErrorLight = Color(0xFFFFFFFF)
private val errorContainerLight = Color(0xFFFFDAD6)
private val onErrorContainerLight = Color(0xFF93000A)
private val backgroundLight = Color(0xFFF5F7F0)
private val onBackgroundLight = Color(0xFF1E2B18)
private val surfaceLight = Color(0xFFF5F7F0)
private val onSurfaceLight = Color(0xFF1E2B18)
private val surfaceVariantLight = Color(0xFFDFE5D7)
private val onSurfaceVariantLight = Color(0xFF474843)
private val outlineLight = Color(0xFF788076)
private val outlineVariantLight = Color(0xFFC3CDBA)
private val scrimLight = Color(0xFF000000)
private val inverseSurfaceLight = Color(0xFF2A322D)
private val inverseOnSurfaceLight = Color(0xFFEEF2EA)
private val inversePrimaryLight = Color(0xFF6F8266)
private val surfaceDimLight = Color(0xFFD5DDD3)
private val surfaceBrightLight = Color(0xFFF5F7F0)
private val surfaceContainerLowestLight = Color(0xFFFFFFFF)
private val surfaceContainerLowLight = Color(0xFFEFF3EA)
private val surfaceContainerLight = Color(0xFFE9EEE4)
private val surfaceContainerHighLight = Color(0xFFE3E9DE)
private val surfaceContainerHighestLight = Color(0xFFDDE3D8)

private val verrouilleDarkScheme = darkColorScheme(
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

private val verrouilleLightScheme = lightColorScheme(
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

package `in`.simply.hisaab.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Tokens that have no Material 3 role (key surface, caution) are read through this. */
public val LocalHisaabColors = staticCompositionLocalOf { HisaabColors.Light }

/** Swap to [HisaabTypeScale.LargeText] when large-text mode ships. */
public val LocalHisaabTypeScale = staticCompositionLocalOf { HisaabTypeScale.Standard }

@Composable
public fun HisaabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    typeScale: HisaabTypeScale = HisaabTypeScale.Standard,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) HisaabColors.Dark else HisaabColors.Light
    CompositionLocalProvider(
        LocalHisaabColors provides colors,
        LocalHisaabTypeScale provides typeScale,
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterial(darkTheme),
            typography = typeScale.toMaterial(),
            content = content,
        )
    }
}

private fun HisaabColors.toMaterial(dark: Boolean) =
    if (dark) {
        darkColorScheme(
            primary = primary, onPrimary = onPrimary,
            primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
            background = background, onBackground = onSurface,
            surface = background, onSurface = onSurface,
            surfaceContainer = surfaceContainer, onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            error = error, onError = onError,
            errorContainer = errorContainer, onErrorContainer = onErrorContainer,
        )
    } else {
        lightColorScheme(
            primary = primary, onPrimary = onPrimary,
            primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
            background = background, onBackground = onSurface,
            surface = background, onSurface = onSurface,
            surfaceContainer = surfaceContainer, onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            error = error, onError = onError,
            errorContainer = errorContainer, onErrorContainer = onErrorContainer,
        )
    }

private fun HisaabTextStyleToken.style(): TextStyle = TextStyle(
    fontSize = sizeSp.sp,
    lineHeight = lineHeightSp.sp,
    fontWeight = FontWeight(weight),
    color = Color.Unspecified,
)

private fun HisaabTypeScale.toMaterial() = Typography(
    displayLarge = numberXl.style(),
    displayMedium = numberL.style(),
    displaySmall = numberM.style(),
    titleLarge = title.style(),
    bodyLarge = bodyL.style(),
    bodyMedium = body.style(),
    labelLarge = label.style(),
    labelSmall = caption.style(),
)

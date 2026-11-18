package `in`.simply.hisaab.core.designsystem

import androidx.compose.ui.graphics.Color

/** Colour roles from the design board ("Design system" artboard). Android 12 dynamic colour is not used. */
public data class HisaabColors(
    val background: Color,
    val surfaceContainer: Color,
    val keySurface: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val cautionContainer: Color,
    val onCautionContainer: Color,
    val cautionIcon: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
) {
    public companion object {
        public val Light: HisaabColors = HisaabColors(
            background = Color(0xFFFBF8F3),
            surfaceContainer = Color(0xFFF1EDE6),
            keySurface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1C1B18),
            onSurfaceVariant = Color(0xFF5A564E),
            outline = Color(0xFF7A766D),
            primary = Color(0xFF0A6B63),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFCDEBE6),
            onPrimaryContainer = Color(0xFF00302C),
            cautionContainer = Color(0xFFFFE3B0),
            onCautionContainer = Color(0xFF4A2F00),
            cautionIcon = Color(0xFF8A5A00),
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B),
        )

        public val Dark: HisaabColors = HisaabColors(
            background = Color(0xFF141311),
            surfaceContainer = Color(0xFF1F1E1B),
            keySurface = Color(0xFF2A2825),
            onSurface = Color(0xFFEAE6DE),
            onSurfaceVariant = Color(0xFFB8B3A8),
            outline = Color(0xFF8F8A80),
            primary = Color(0xFF7FD3C8),
            onPrimary = Color(0xFF00332E),
            primaryContainer = Color(0xFF00504A),
            onPrimaryContainer = Color(0xFFCDEBE6),
            cautionContainer = Color(0xFF5A3F00),
            onCautionContainer = Color(0xFFFFE3B0),
            cautionIcon = Color(0xFFF2C46B),
            error = Color(0xFFF2B8B5),
            onError = Color(0xFF601410),
            errorContainer = Color(0xFF601410),
            onErrorContainer = Color(0xFFF9DEDC),
        )
    }
}

public data class HisaabTextStyleToken(
    val name: String,
    val sizeSp: Int,
    val lineHeightSp: Int,
    val weight: Int,
)

/**
 * Type scale in sp. [LargeText] is the reserved second scale (token swap, no layout rewrite):
 * only number-xl and key labels grow, as written in the build spec.
 */
public data class HisaabTypeScale(
    val numberXl: HisaabTextStyleToken,
    val numberL: HisaabTextStyleToken,
    val numberM: HisaabTextStyleToken,
    val keyLabel: HisaabTextStyleToken,
    val title: HisaabTextStyleToken,
    val bodyL: HisaabTextStyleToken,
    val body: HisaabTextStyleToken,
    val label: HisaabTextStyleToken,
    val caption: HisaabTextStyleToken,
) {
    val all: List<HisaabTextStyleToken>
        get() = listOf(numberXl, numberL, numberM, keyLabel, title, bodyL, body, label, caption)

    public companion object {
        public val Standard: HisaabTypeScale = HisaabTypeScale(
            numberXl = HisaabTextStyleToken("number-xl", 56, 64, 500),
            numberL = HisaabTextStyleToken("number-l", 40, 48, 500),
            numberM = HisaabTextStyleToken("number-m", 28, 36, 400),
            keyLabel = HisaabTextStyleToken("key-label", 28, 32, 500),
            title = HisaabTextStyleToken("title", 22, 28, 500),
            bodyL = HisaabTextStyleToken("body-l", 18, 26, 400),
            body = HisaabTextStyleToken("body", 16, 24, 400),
            label = HisaabTextStyleToken("label", 14, 20, 500),
            caption = HisaabTextStyleToken("caption", 12, 16, 400),
        )

        public val LargeText: HisaabTypeScale = Standard.copy(
            numberXl = HisaabTextStyleToken("number-xl", 72, 80, 500),
            keyLabel = HisaabTextStyleToken("key-label", 36, 40, 500),
        )
    }
}

public object HisaabDimens {
    public const val MinTouchTargetDp: Int = 48
    public val Spacing: List<Int> = listOf(4, 8, 12, 16, 24, 32, 48)
}

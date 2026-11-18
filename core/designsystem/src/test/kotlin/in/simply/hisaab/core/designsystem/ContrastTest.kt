package `in`.simply.hisaab.core.designsystem

import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertTrue

/** WCAG 2.x contrast ratio between two opaque colours. */
internal fun contrast(a: Color, b: Color): Double {
    fun channel(c: Float): Double {
        val v = c.toDouble()
        return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }
    fun luminance(c: Color) = 0.2126 * channel(c.red) + 0.7152 * channel(c.green) + 0.0722 * channel(c.blue)
    val (hi, lo) = luminance(a).let { la -> luminance(b).let { lb -> maxOf(la, lb) to minOf(la, lb) } }
    return (hi + 0.05) / (lo + 0.05)
}

class ContrastTest {

    private val themes = mapOf("light" to HisaabColors.Light, "dark" to HisaabColors.Dark)

    @Test
    fun `every text pair meets AA 4_5 to 1 in both themes`() {
        themes.forEach { (name, c) ->
            val textPairs = mapOf(
                "onSurface on background" to (c.onSurface to c.background),
                "onSurface on surfaceContainer" to (c.onSurface to c.surfaceContainer),
                "onSurface on keySurface" to (c.onSurface to c.keySurface),
                "onSurfaceVariant on background" to (c.onSurfaceVariant to c.background),
                "onPrimary on primary" to (c.onPrimary to c.primary),
                "onPrimaryContainer on primaryContainer" to (c.onPrimaryContainer to c.primaryContainer),
                "onCautionContainer on cautionContainer" to (c.onCautionContainer to c.cautionContainer),
                "onErrorContainer on errorContainer" to (c.onErrorContainer to c.errorContainer),
                "error on background" to (c.error to c.background),
                "primary on background" to (c.primary to c.background),
            )
            textPairs.forEach { (label, pair) ->
                val ratio = contrast(pair.first, pair.second)
                assertTrue(ratio >= 4.5, "$name: $label is ${"%.2f".format(ratio)}:1, needs 4.5:1")
            }
        }
    }

    @Test
    fun `outlines and the caution icon meet 3 to 1 in both themes`() {
        themes.forEach { (name, c) ->
            val uiPairs = mapOf(
                "outline on background" to (c.outline to c.background),
                "cautionIcon on cautionContainer" to (c.cautionIcon to c.cautionContainer),
            )
            uiPairs.forEach { (label, pair) ->
                val ratio = contrast(pair.first, pair.second)
                assertTrue(ratio >= 3.0, "$name: $label is ${"%.2f".format(ratio)}:1, needs 3:1")
            }
        }
    }
}

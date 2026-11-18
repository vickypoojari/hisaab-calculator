package `in`.simply.hisaab.feature.calculator

import `in`.simply.hisaab.parser.AmountWords
import `in`.simply.hisaab.parser.CalcResult
import `in`.simply.hisaab.parser.Calculator
import `in`.simply.hisaab.parser.ErrorReason
import `in`.simply.hisaab.parser.GroupingStyle
import `in`.simply.hisaab.parser.NumberFormatter
import java.math.BigDecimal

/** What the display zone shows for an expression. */
data class CalculatorDisplay(
    /** The expression with Indian grouping and spaced operators, e.g. "1,20,000 + 85,000". */
    val expressionText: String,
    /** Live result ("0" when empty). Blank when [error] is set. */
    val resultText: String,
    /** "2.05 lakh" caption, from one lakh up. */
    val wordLine: String?,
    val error: ErrorReason?,
    val isEmpty: Boolean,
    val equalsEnabled: Boolean,
) {
    companion object {
        private const val OPERATORS = "+−×÷"

        fun from(expression: String): CalculatorDisplay {
            val shown = format(expression)
            // A trailing operator or half-typed decimal point shows the value of what came before it.
            val readable = expression.trimEnd { it in OPERATORS || it == '.' }
            val result = if (readable.isEmpty() || readable == "−") CalcResult.NothingToCalculate else Calculator.evaluate(readable)
            return when (result) {
                is CalcResult.Success -> CalculatorDisplay(
                    expressionText = shown,
                    resultText = NumberFormatter.format(result.value, GroupingStyle.INDIAN),
                    wordLine = AmountWords.of(result.value),
                    error = null,
                    isEmpty = false,
                    equalsEnabled = true,
                )
                is CalcResult.Error -> failed(shown, result.reason)
                // The keypad cannot produce an ambiguous reading; treat it as unreadable if text ever does.
                is CalcResult.Ambiguous -> failed(shown, ErrorReason.UNREADABLE)
                CalcResult.NothingToCalculate -> CalculatorDisplay(
                    expressionText = shown,
                    resultText = "0",
                    wordLine = null,
                    error = null,
                    isEmpty = expression.isEmpty(),
                    equalsEnabled = false,
                )
            }
        }

        private fun failed(shown: String, reason: ErrorReason) = CalculatorDisplay(
            expressionText = shown,
            resultText = "",
            wordLine = null,
            error = reason,
            isEmpty = false,
            equalsEnabled = false,
        )

        /** Groups each number Indian style and spaces the operators; a leading minus hugs its number. */
        private fun format(expression: String): String {
            val out = StringBuilder()
            var i = 0
            while (i < expression.length) {
                val c = expression[i]
                when {
                    c.isDigit() || c == '.' -> {
                        val end = (i until expression.length).firstOrNull { !expression[it].isDigit() && expression[it] != '.' } ?: expression.length
                        out.append(group(expression.substring(i, end)))
                        i = end
                        continue
                    }
                    c == '%' -> out.append('%')
                    i == 0 -> out.append(c)
                    else -> out.append(' ').append(c).append(' ')
                }
                i++
            }
            return out.toString().trimEnd()
        }

        private fun group(number: String): String {
            val integer = number.substringBefore('.').ifEmpty { "0" }
            val rest = if ('.' in number) "." + number.substringAfter('.') else ""
            return NumberFormatter.format(BigDecimal(integer), GroupingStyle.INDIAN) + rest
        }
    }
}

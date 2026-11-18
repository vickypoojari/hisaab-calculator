package `in`.simply.hisaab.feature.selection

import `in`.simply.hisaab.parser.CalcResult
import `in`.simply.hisaab.parser.Calculator
import `in`.simply.hisaab.parser.GroupingStyle
import `in`.simply.hisaab.parser.NumberFormatter
import java.math.BigDecimal

/** Turns selected text into the sheet state. Pure Kotlin so it is tested on the JVM. */
object SelectionPresenter {

    fun present(selectedText: String, canReplace: Boolean): SelectionUiState =
        when (val result = Calculator.evaluate(selectedText)) {
            is CalcResult.Success -> {
                val out = outputs(result.value, result.hasCurrency, selectedText, canReplace)
                SelectionUiState.Success(
                    readAs = result.readAs,
                    ignoredWords = result.ignoredWords,
                    resultText = out.resultText,
                    copyText = out.copyText,
                    replaceText = out.replaceText,
                )
            }
            is CalcResult.Ambiguous -> SelectionUiState.Ambiguous(
                readings = result.readings.map { reading ->
                    val out = outputs(reading.value, result.hasCurrency, selectedText, canReplace)
                    ReadingUi(reading.readAs, out.resultText, out.copyText, out.replaceText)
                },
                selectedIndex = 0,
                ignoredWords = result.ignoredWords,
                canReplace = canReplace,
            )
            CalcResult.NothingToCalculate -> SelectionUiState.NothingToCalculate(selectedText)
            is CalcResult.Error -> SelectionUiState.Error(result.reason, selectedText)
        }

    private class Outputs(val resultText: String, val copyText: String, val replaceText: String?)

    /**
     * Result is always shown grouped. Copy is plain digits so it pastes into UPI and bank fields.
     * Replace keeps ₹ and grouping only if the selected text used them.
     */
    private fun outputs(value: BigDecimal, currency: Boolean, source: String, canReplace: Boolean): Outputs {
        val groupedInSource = source.contains(',')
        return Outputs(
            resultText = money(value, currency, grouped = true),
            copyText = value.toPlainString(),
            replaceText = if (canReplace) money(value, currency, grouped = groupedInSource) else null,
        )
    }

    /** Sign goes before ₹ and digits: "−₹1,200". */
    private fun money(value: BigDecimal, currency: Boolean, grouped: Boolean): String {
        val sign = if (value.signum() < 0) "−" else ""
        val magnitude = value.abs()
        val digits = if (grouped) NumberFormatter.format(magnitude, GroupingStyle.INDIAN) else magnitude.toPlainString()
        return sign + (if (currency) "₹" else "") + digits
    }
}

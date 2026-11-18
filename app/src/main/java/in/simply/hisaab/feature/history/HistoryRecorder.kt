package `in`.simply.hisaab.feature.history

import `in`.simply.hisaab.feature.calculator.CalculatorDisplay
import `in`.simply.hisaab.feature.selection.SelectionUiState
import `in`.simply.hisaab.parser.CalcResult
import `in`.simply.hisaab.parser.Calculator

/** Decides what is worth saving, and builds the entry. Pure Kotlin. */
object HistoryRecorder {
    private const val OPERATIONS = "+−×÷%"
    private const val CALCULATOR_OPERATORS = "+−×÷"

    /** The sum on the calculator when `=` is pressed. Lone numbers and failed sums are not saved. */
    fun fromCalculator(expression: String, nowMillis: Long): NewHistoryEntry? {
        val readable = expression.trimEnd { it in CALCULATOR_OPERATORS || it == '.' }
        if (!isOperation(readable)) return null
        val result = Calculator.evaluate(readable) as? CalcResult.Success ?: return null
        val display = CalculatorDisplay.from(readable)
        return NewHistoryEntry(
            expression = display.expressionText,
            result = display.resultText,
            value = result.value.toPlainString(),
            source = HistorySource.CALCULATOR,
            createdAt = nowMillis,
        )
    }

    /** A successful result from the selection sheet; for an ambiguous one, the reading that is chosen. */
    fun fromSelection(state: SelectionUiState, nowMillis: Long): NewHistoryEntry? = when (state) {
        is SelectionUiState.Success -> selected(state.readAs, state.resultText, state.copyText, nowMillis)
        is SelectionUiState.Ambiguous -> state.selected.let { selected(it.readAs, it.resultText, it.copyText, nowMillis) }
        is SelectionUiState.NothingToCalculate, is SelectionUiState.Error -> null
    }

    private fun selected(readAs: String, result: String, value: String, nowMillis: Long): NewHistoryEntry? =
        if (isOperation(readAs)) NewHistoryEntry(readAs, result, value, HistorySource.SELECTED_TEXT, nowMillis) else null

    /** True when there is an operator or percent beyond a leading sign: a number on its own is not a sum. */
    private fun isOperation(text: String): Boolean = text.removePrefix("−").removePrefix("-").any { it in OPERATIONS }
}

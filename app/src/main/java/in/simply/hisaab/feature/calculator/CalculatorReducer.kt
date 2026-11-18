package `in`.simply.hisaab.feature.calculator

import `in`.simply.hisaab.feature.calculator.CalculatorKey.Kind
import `in`.simply.hisaab.parser.CalcResult
import `in`.simply.hisaab.parser.Calculator

/**
 * What each key does to the expression text. The expression is kept raw (ASCII digits, `.`,
 * `+ − × ÷ %`); grouping is applied only when it is shown. Pure Kotlin, tested on the JVM.
 */
object CalculatorReducer {
    private const val OPERATORS = "+−×÷"
    private const val MAX_DIGITS = 15

    fun press(expression: String, key: CalculatorKey): String = when {
        key == CalculatorKey.CLEAR -> ""
        key == CalculatorKey.BACKSPACE -> expression.dropLast(1)
        key == CalculatorKey.EQUALS -> equals(expression)
        key == CalculatorKey.PERCENT -> percent(expression)
        key == CalculatorKey.DECIMAL -> decimal(expression)
        key.kind == Kind.OPERATOR -> operator(expression, key.label.single())
        else -> digits(expression, key.label)
    }

    private fun operator(expression: String, symbol: Char): String = when {
        expression.isEmpty() -> if (symbol == '−') "−" else expression
        expression == "−" -> expression
        expression.last() in OPERATORS || expression.last() == '.' -> expression.dropLast(1) + symbol
        else -> expression + symbol
    }

    private fun percent(expression: String): String =
        if (expression.lastOrNull()?.isDigit() == true) expression + "%" else expression

    private fun decimal(expression: String): String {
        if (expression.lastOrNull() == '%') return expression
        val number = currentNumber(expression)
        return when {
            '.' in number -> expression
            number.isEmpty() -> expression + "0."
            else -> "$expression."
        }
    }

    private fun digits(expression: String, typed: String): String {
        if (expression.lastOrNull() == '%') return expression
        val number = currentNumber(expression)
        if (number.count { it.isDigit() } + typed.length > MAX_DIGITS) return expression
        return when {
            number == "0" -> if (typed.all { it == '0' }) expression else expression.dropLast(1) + typed
            number.isEmpty() && typed == "00" -> expression + "0"
            else -> expression + typed
        }
    }

    /** The digits and decimal point at the very end: the number being typed. */
    private fun currentNumber(expression: String): String = expression.takeLastWhile { it.isDigit() || it == '.' }

    private fun equals(expression: String): String {
        val readable = expression.trimEnd { it in OPERATORS || it == '.' }
        if (readable.isEmpty() || readable == "−") return expression
        val result = Calculator.evaluate(readable)
        return if (result is CalcResult.Success) result.value.toPlainString().replace('-', '−') else expression
    }
}

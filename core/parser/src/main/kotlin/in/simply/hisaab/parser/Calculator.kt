package `in`.simply.hisaab.parser

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

public sealed interface CalcResult {
    public data class Success(
        val value: BigDecimal,
        /** The expression as the app understood it, e.g. "₹1,200 + ₹450". */
        val readAs: String,
        /** Words that were not part of the sum, in the order they appeared. */
        val ignoredWords: List<String>,
        /** True when the source text marked at least one amount with ₹. */
        val hasCurrency: Boolean,
    ) : CalcResult

    /** The text held no number, so there is nothing to compute. */
    public data object NothingToCalculate : CalcResult

    /** A number was found but the sum cannot be computed. */
    public data class Error(val reason: ErrorReason) : CalcResult
}

public enum class ErrorReason { DIVIDE_BY_ZERO, UNREADABLE }

public object Calculator {
    private val DIVISION = MathContext(10, RoundingMode.HALF_UP)

    public fun evaluate(text: String): CalcResult {
        val scan = Scanner.scan(text)
        val tokens = scan.tokens
        if (tokens.none { it is Token.Num }) return CalcResult.NothingToCalculate
        if (!isAlternating(tokens)) return CalcResult.Error(ErrorReason.UNREADABLE)

        val value = compute(tokens) ?: return CalcResult.Error(ErrorReason.DIVIDE_BY_ZERO)
        return CalcResult.Success(
            value = value,
            readAs = tokens.joinToString(" ") { it.display },
            ignoredWords = scan.ignoredWords,
            hasCurrency = tokens.any { it is Token.Num && it.currency },
        )
    }

    /** A sum is number, operator, number, ... starting and ending with a number. */
    private fun isAlternating(tokens: List<Token>): Boolean =
        tokens.size % 2 == 1 && tokens.withIndex().all { (i, t) -> (i % 2 == 0) == (t is Token.Num) }

    /** Resolves × and ÷ first, then + and −, left to right. Null means division by zero. */
    private fun compute(tokens: List<Token>): BigDecimal? {
        val numbers = tokens.filterIsInstance<Token.Num>().map { it.value }
        val operators = tokens.filterIsInstance<Token.Op>().map { it.symbol }

        val terms = mutableListOf(numbers.first())
        val addOps = mutableListOf<Char>()
        operators.forEachIndexed { i, op ->
            val next = numbers[i + 1]
            when (op) {
                '×' -> terms[terms.lastIndex] = terms.last() * next
                '÷' -> {
                    if (next.signum() == 0) return null
                    terms[terms.lastIndex] = terms.last().divide(next, DIVISION)
                }
                else -> {
                    addOps += op
                    terms += next
                }
            }
        }
        var total = terms.first()
        addOps.forEachIndexed { i, op ->
            total = if (op == '+') total + terms[i + 1] else total - terms[i + 1]
        }
        return total.stripTrailingZeros().let { if (it.scale() < 0) it.setScale(0) else it }
    }
}

internal sealed interface Token {
    val display: String

    data class Num(val value: BigDecimal, val text: String, val currency: Boolean) : Token {
        override val display: String get() = (if (currency) "₹" else "") + text
    }

    data class Op(val symbol: Char) : Token {
        override val display: String get() = symbol.toString()
    }
}

internal data class ScanResult(val tokens: List<Token>, val ignoredWords: List<String>)

internal object Scanner {
    private val NUMBER = Regex("""\d+(?:,\d+)*(?:\.\d+)?|\.\d+""")

    fun scan(text: String): ScanResult {
        val tokens = mutableListOf<Token>()
        val ignored = mutableListOf<String>()
        var pendingCurrency = false
        var index = 0
        while (index < text.length) {
            val char = text[index]
            val number = NUMBER.matchAt(text, index)
            when {
                number != null -> {
                    tokens += Token.Num(BigDecimal(number.value.replace(",", "")), number.value, pendingCurrency)
                    pendingCurrency = false
                    index = number.range.last + 1
                }
                char == '₹' -> {
                    pendingCurrency = true
                    index++
                }
                char.isLetter() -> {
                    var end = index
                    while (end < text.length && text[end].isLetter()) end++
                    val word = text.substring(index, end)
                    if (word == "x" || word == "X") tokens += Token.Op('×') else ignored += word
                    index = end
                }
                else -> {
                    operatorFor(char)?.let { tokens += Token.Op(it) }
                    index++
                }
            }
        }
        return ScanResult(tokens, ignored)
    }

    private fun operatorFor(char: Char): Char? = when (char) {
        '+' -> '+'
        '-', '−' -> '−'
        '×', '*' -> '×'
        '÷', '/' -> '÷'
        else -> null
    }
}

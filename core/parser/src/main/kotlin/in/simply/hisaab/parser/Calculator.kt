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

    /** The text can mean more than one thing. [readings] is ordered most likely first. */
    public data class Ambiguous(
        val readings: List<Reading>,
        val ignoredWords: List<String>,
        val hasCurrency: Boolean,
    ) : CalcResult

    /** The text held no number, so there is nothing to compute. */
    public data object NothingToCalculate : CalcResult

    /** A number was found but the sum cannot be computed. */
    public data class Error(val reason: ErrorReason) : CalcResult
}

public enum class ErrorReason { DIVIDE_BY_ZERO, UNREADABLE }

/** One possible reading of ambiguous text: how it was read and what it comes to. */
public data class Reading(val readAs: String, val value: BigDecimal)

public object Calculator {
    private val DIVISION = MathContext(10, RoundingMode.HALF_UP)

    public fun evaluate(text: String): CalcResult {
        val scan = Scanner.scan(text)
        val tokens = scan.tokens
        if (tokens.none { it is Token.Num }) return CalcResult.NothingToCalculate
        if (!isAlternating(tokens)) return ambiguity(scan) ?: CalcResult.Error(ErrorReason.UNREADABLE)

        val value = compute(tokens) ?: return CalcResult.Error(ErrorReason.DIVIDE_BY_ZERO)
        return CalcResult.Success(
            value = value,
            readAs = tokens.readAs(),
            ignoredWords = scan.ignoredWords,
            hasCurrency = tokens.hasCurrency(),
        )
    }

    /**
     * An amount directly followed by a percent ("₹2,50,000 12%") has no operator, so it can be
     * read as × or as +. Exactly one such gap is supported; anything messier is unreadable.
     */
    private fun ambiguity(scan: ScanResult): CalcResult? {
        val tokens = scan.tokens
        val gaps = (0 until tokens.size - 1).filter { i ->
            val left = tokens[i]
            val right = tokens[i + 1]
            left is Token.Num && right is Token.Num && right.percent
        }
        val gap = gaps.singleOrNull() ?: return null
        val readings = listOf('×', '+').mapNotNull { symbol ->
            val candidate = tokens.toMutableList().apply { add(gap + 1, Token.Op(symbol)) }
            if (!isAlternating(candidate)) return null
            compute(candidate)?.let { Reading(candidate.readAs(), it) }
        }
        if (readings.isEmpty()) return null
        return CalcResult.Ambiguous(readings, scan.ignoredWords, tokens.hasCurrency())
    }

    private fun List<Token>.readAs(): String = joinToString(" ") { it.display }

    private fun List<Token>.hasCurrency(): Boolean = any { it is Token.Num && it.currency }

    /** A sum is number, operator, number, ... starting and ending with a number. */
    private fun isAlternating(tokens: List<Token>): Boolean =
        tokens.size % 2 == 1 && tokens.withIndex().all { (i, t) -> (i % 2 == 0) == (t is Token.Num) }

    /**
     * Resolves × and ÷ first, then + and −, left to right. Percent rules (design board):
     * in × and ÷ a percent is p/100; after + or − it is p% of the running total.
     * Null means division by zero.
     */
    private fun compute(tokens: List<Token>): BigDecimal? {
        val numbers = tokens.filterIsInstance<Token.Num>()
        val operators = tokens.filterIsInstance<Token.Op>().map { it.symbol }

        // A term is a product/quotient chain. `bare` is true while it is still one lone percent.
        class Term(var value: BigDecimal, var bare: Boolean)
        val terms = mutableListOf(Term(numbers.first().value, numbers.first().percent))
        val addOps = mutableListOf<Char>()
        operators.forEachIndexed { i, op ->
            val next = numbers[i + 1]
            val operand = if (next.percent) next.value.movePointLeft(2) else next.value
            when (op) {
                '×', '÷' -> {
                    val term = terms.last()
                    if (term.bare) {
                        term.value = term.value.movePointLeft(2)
                        term.bare = false
                    }
                    if (op == '÷' && operand.signum() == 0) return null
                    term.value = if (op == '×') term.value * operand else term.value.divide(operand, DIVISION)
                }
                else -> {
                    addOps += op
                    terms += Term(next.value, next.percent)
                }
            }
        }
        val first = terms.first()
        var total = if (first.bare) first.value.movePointLeft(2) else first.value
        addOps.forEachIndexed { i, op ->
            val term = terms[i + 1]
            val delta = if (term.bare) total * term.value.movePointLeft(2) else term.value
            total = if (op == '+') total + delta else total - delta
        }
        return total.stripTrailingZeros().let { if (it.scale() < 0) it.setScale(0) else it }
    }
}

internal sealed interface Token {
    val display: String

    data class Num(val value: BigDecimal, val text: String, val currency: Boolean, val percent: Boolean) : Token {
        override val display: String get() = (if (currency) "₹" else "") + text + (if (percent) "%" else "")
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
                    var end = number.range.last + 1
                    var after = end
                    while (after < text.length && text[after] == ' ') after++
                    val percent = after < text.length && text[after] == '%'
                    if (percent) end = after + 1
                    tokens += Token.Num(
                        BigDecimal(number.value.replace(",", "")), number.value, pendingCurrency, percent,
                    )
                    pendingCurrency = false
                    index = end
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

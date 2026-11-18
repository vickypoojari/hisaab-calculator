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
}

public object Calculator {
    private val NUMBER = Regex("""\d+(?:,\d+)*(?:\.\d+)?|\.\d+""")
    private val DIVISION = MathContext(10, RoundingMode.HALF_UP)

    public fun evaluate(text: String): CalcResult {
        val numbers = mutableListOf<BigDecimal>()
        val operators = mutableListOf<Char>()
        val readAs = mutableListOf<String>()
        val ignored = mutableListOf<String>()
        var hasCurrency = false
        var pendingCurrency = false
        var index = 0
        while (index < text.length) {
            val char = text[index]
            val number = NUMBER.matchAt(text, index)
            when {
                number != null -> {
                    numbers += BigDecimal(number.value.replace(",", ""))
                    readAs += (if (pendingCurrency) "₹" else "") + number.value
                    pendingCurrency = false
                    index = number.range.last + 1
                }
                char == '₹' -> {
                    hasCurrency = true
                    pendingCurrency = true
                    index++
                }
                char.isLetter() -> {
                    val end = text.indexOfFirst(index) { !it.isLetter() }
                    val word = text.substring(index, end)
                    if (word == "x" || word == "X") {
                        operators += '×'
                        readAs += "×"
                    } else {
                        ignored += word
                    }
                    index = end
                }
                else -> {
                    operatorFor(char)?.let {
                        operators += it
                        readAs += it.toString()
                    }
                    index++
                }
            }
        }
        if (numbers.isEmpty()) return CalcResult.NothingToCalculate
        return CalcResult.Success(
            value = compute(numbers, operators),
            readAs = readAs.joinToString(" "),
            ignoredWords = ignored,
            hasCurrency = hasCurrency,
        )
    }

    private fun String.indexOfFirst(from: Int, predicate: (Char) -> Boolean): Int {
        var i = from
        while (i < length && !predicate(this[i])) i++
        return i
    }

    private fun operatorFor(char: Char): Char? = when (char) {
        '+' -> '+'
        '-', '−' -> '−'
        '×', '*' -> '×'
        '÷', '/' -> '÷'
        else -> null
    }

    /** Resolves × and ÷ first, then + and −, left to right. */
    private fun compute(numbers: List<BigDecimal>, operators: List<Char>): BigDecimal {
        val terms = mutableListOf(numbers.first())
        val addOps = mutableListOf<Char>()
        operators.forEachIndexed { i, op ->
            val next = numbers[i + 1]
            when (op) {
                '×' -> terms[terms.lastIndex] = terms.last() * next
                '÷' -> terms[terms.lastIndex] = terms.last().divide(next, DIVISION)
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

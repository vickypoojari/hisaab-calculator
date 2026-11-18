package `in`.simply.hisaab.parser

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

public sealed interface CalcResult {
    public data class Success(val value: BigDecimal) : CalcResult
}

public object Calculator {
    private val NUMBER = Regex("""\d+(?:,\d+)*(?:\.\d+)?|\.\d+""")
    private val DIVISION = MathContext(10, RoundingMode.HALF_UP)

    public fun evaluate(text: String): CalcResult {
        val numbers = mutableListOf<BigDecimal>()
        val operators = mutableListOf<Char>()
        var index = 0
        while (index < text.length) {
            val match = NUMBER.matchAt(text, index)
            if (match != null) {
                numbers += BigDecimal(match.value.replace(",", ""))
                index = match.range.last + 1
                continue
            }
            operatorFor(text[index])?.let { operators += it }
            index++
        }
        return CalcResult.Success(compute(numbers, operators))
    }

    private fun operatorFor(char: Char): Char? = when (char) {
        '+' -> '+'
        '-', '−' -> '−'
        '×', 'x', 'X', '*' -> '×'
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

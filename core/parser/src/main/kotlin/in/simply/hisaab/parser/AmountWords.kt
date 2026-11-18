package `in`.simply.hisaab.parser

import java.math.BigDecimal
import java.math.RoundingMode

/** The "2.05 lakh" caption under a result. Null when the amount is under one lakh. */
public object AmountWords {
    private val LAKH = BigDecimal(100_000)
    private val CRORE = BigDecimal(10_000_000)
    private val HUNDRED = BigDecimal(100)

    public fun of(value: BigDecimal): String? {
        val magnitude = value.abs()
        if (magnitude < LAKH) return null
        val sign = if (value.signum() < 0) "−" else ""
        if (magnitude < CRORE) {
            val lakhs = inUnits(magnitude, LAKH)
            // 99.999 lakh rounds to 100.00: that is a crore, not "100 lakh".
            if (lakhs < HUNDRED) return "$sign${plain(lakhs)} lakh"
        }
        return "$sign${plain(inUnits(magnitude, CRORE))} crore"
    }

    private fun inUnits(value: BigDecimal, unit: BigDecimal): BigDecimal = value.divide(unit, 2, RoundingMode.HALF_UP)

    private fun plain(value: BigDecimal): String {
        val trimmed = value.stripTrailingZeros().let { if (it.scale() < 0) it.setScale(0) else it }
        return NumberFormatter.format(trimmed, GroupingStyle.INDIAN)
    }
}

package `in`.simply.hisaab.parser

import java.math.BigDecimal

public enum class GroupingStyle { INDIAN, INTERNATIONAL }

public object NumberFormatter {
    public fun format(value: BigDecimal, style: GroupingStyle = GroupingStyle.INDIAN): String {
        val plain = value.toPlainString()
        val integerPart = plain.substringBefore('.')
        val fraction = plain.substringAfter('.', missingDelimiterValue = "")
        val grouped = group(integerPart, style)
        return if (fraction.isEmpty()) grouped else "$grouped.$fraction"
    }

    private fun group(digits: String, style: GroupingStyle): String {
        if (digits.length <= 3) return digits
        val head = digits.dropLast(3)
        val tail = digits.takeLast(3)
        val size = if (style == GroupingStyle.INDIAN) 2 else 3
        return head.reversed().chunked(size).joinToString(",").reversed() + "," + tail
    }
}

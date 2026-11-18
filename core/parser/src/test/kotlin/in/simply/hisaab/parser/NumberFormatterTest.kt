package `in`.simply.hisaab.parser

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class NumberFormatterTest {

    private fun indian(value: String): String =
        NumberFormatter.format(BigDecimal(value), GroupingStyle.INDIAN)

    @Test
    fun `indian grouping uses last three digits then pairs`() {
        assertEquals("999", indian("999"))
        assertEquals("1,000", indian("1000"))
        assertEquals("12,345", indian("12345"))
        assertEquals("1,00,000", indian("100000"))
        assertEquals("12,34,567", indian("1234567"))
        assertEquals("1,23,45,678", indian("12345678"))
    }

    @Test
    fun `decimals are kept after the grouped integer part`() {
        assertEquals("12,34,567.89", indian("1234567.89"))
    }

    @Test
    fun `negative numbers keep the sign in front and group the digits`() {
        // Design board: leading U+2212 minus, not red, not a hyphen.
        assertEquals("−5", indian("-5"))
        assertEquals("−1,200", indian("-1200"))
        assertEquals("−12,345", indian("-12345"))
        assertEquals("−12,34,567.5", indian("-1234567.5"))
    }

    @Test
    fun `international grouping uses groups of three`() {
        assertEquals(
            "1,234,567.89",
            NumberFormatter.format(BigDecimal("1234567.89"), GroupingStyle.INTERNATIONAL),
        )
    }
}

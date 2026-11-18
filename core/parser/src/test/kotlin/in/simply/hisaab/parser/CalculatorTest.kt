package `in`.simply.hisaab.parser

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CalculatorTest {

    private fun valueOf(text: String): BigDecimal {
        val result = Calculator.evaluate(text)
        assertIs<CalcResult.Success>(result, "expected success for \"$text\" but got $result")
        return result.value
    }

    @Test
    fun `adds numbers written with Indian grouping`() {
        assertEquals(BigDecimal("205000"), valueOf("1,20,000 + 85,000"))
    }

    @Test
    fun `multiplication binds tighter than addition`() {
        assertEquals(BigDecimal("14"), valueOf("2 + 3 × 4"))
    }

    @Test
    fun `accepts x star and slash as operators and both minus signs`() {
        assertEquals(BigDecimal("12"), valueOf("3 x 4"))
        assertEquals(BigDecimal("12"), valueOf("3 * 4"))
        assertEquals(BigDecimal("5"), valueOf("10 / 2"))
        assertEquals(BigDecimal("6"), valueOf("10 − 4"))
        assertEquals(BigDecimal("6"), valueOf("10 - 4"))
    }

    @Test
    fun `division keeps decimals up to ten significant digits`() {
        assertEquals(BigDecimal("0.3333333333"), valueOf("1 ÷ 3"))
    }

    @Test
    fun `reads a sum out of messy text and reports what it ignored`() {
        val result = Calculator.evaluate("Order Zomato ₹1,200 + ₹450 delivery")

        assertIs<CalcResult.Success>(result)
        assertEquals(BigDecimal("1650"), result.value)
        assertEquals("₹1,200 + ₹450", result.readAs)
        // Every non-numeric word is ignored, in the order it appears. (The design board lists only
        // "Zomato, delivery"; the rule has to be deterministic, so "Order" is ignored too.)
        assertEquals(listOf("Order", "Zomato", "delivery"), result.ignoredWords)
        assertEquals(true, result.hasCurrency)
    }

    @Test
    fun `plain numbers have no currency and nothing ignored`() {
        val result = Calculator.evaluate("1,20,000 + 85,000")

        assertIs<CalcResult.Success>(result)
        assertEquals("1,20,000 + 85,000", result.readAs)
        assertEquals(emptyList(), result.ignoredWords)
        assertEquals(false, result.hasCurrency)
    }

    @Test
    fun `a standalone x is an operator but x inside a word is not`() {
        val result = Calculator.evaluate("xerox 3 x 4")

        assertIs<CalcResult.Success>(result)
        assertEquals(BigDecimal("12"), result.value)
        assertEquals(listOf("xerox"), result.ignoredWords)
    }
}

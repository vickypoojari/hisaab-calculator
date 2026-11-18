package `in`.simply.hisaab.parser

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** A minus at the very start is a sign (so a negative result can be carried on), not an operator. */
class LeadingMinusTest {

    private fun success(text: String): CalcResult.Success {
        val result = Calculator.evaluate(text)
        assertIs<CalcResult.Success>(result, "expected success for \"$text\" but got $result")
        return result
    }

    @Test
    fun `a leading minus makes the first number negative`() {
        assertEquals(BigDecimal("-2"), success("−5 + 3").value)
        assertEquals(BigDecimal("-10"), success("-5 × 2").value)
        assertEquals(BigDecimal("-5"), success("−5").value)
    }

    @Test
    fun `the sign is kept in front of the rupee in the reading`() {
        val result = success("−₹1,200 + ₹450")

        assertEquals(BigDecimal("-750"), result.value)
        assertEquals("−₹1,200 + ₹450", result.readAs)
        assertEquals(true, result.hasCurrency)
    }

    @Test
    fun `a leading minus on a percent is a negative fraction`() {
        assertEquals(BigDecimal("-0.12"), success("−12%").value)
    }

    @Test
    fun `a lone minus is still nothing to calculate`() {
        assertEquals(CalcResult.NothingToCalculate, Calculator.evaluate("−"))
    }

    @Test
    fun `only the very first minus is a sign, a second one in a row stays unreadable`() {
        assertEquals(CalcResult.Error(ErrorReason.UNREADABLE), Calculator.evaluate("5 − −3"))
        assertEquals(CalcResult.Error(ErrorReason.UNREADABLE), Calculator.evaluate("− − 5"))
    }
}

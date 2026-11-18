package `in`.simply.hisaab.parser

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class NothingToCalculateTest {

    @Test
    fun `text without any number has nothing to calculate`() {
        assertEquals(CalcResult.NothingToCalculate, Calculator.evaluate("Reaching by evening, call me"))
    }

    @Test
    fun `empty and blank input has nothing to calculate`() {
        assertEquals(CalcResult.NothingToCalculate, Calculator.evaluate(""))
        assertEquals(CalcResult.NothingToCalculate, Calculator.evaluate("   "))
    }

    @Test
    fun `a rupee sign or operators alone are not a sum`() {
        assertEquals(CalcResult.NothingToCalculate, Calculator.evaluate("₹"))
        assertEquals(CalcResult.NothingToCalculate, Calculator.evaluate("+ × −"))
    }

    @Test
    fun `a single number is a valid result`() {
        val result = Calculator.evaluate("Total ₹450")

        assertIs<CalcResult.Success>(result)
        assertEquals(BigDecimal("450"), result.value)
        assertEquals("₹450", result.readAs)
    }
}

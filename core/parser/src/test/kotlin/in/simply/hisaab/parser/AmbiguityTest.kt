package `in`.simply.hisaab.parser

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AmbiguityTest {

    private fun ambiguous(text: String): CalcResult.Ambiguous {
        val result = Calculator.evaluate(text)
        assertIs<CalcResult.Ambiguous>(result, "expected Ambiguous for \"$text\" but got $result")
        return result
    }

    @Test
    fun `an amount followed by a bare percent can mean times or plus`() {
        val result = ambiguous("Loan ₹2,50,000 12%")

        assertEquals(
            listOf(
                Reading("₹2,50,000 × 12%", BigDecimal("30000")),
                Reading("₹2,50,000 + 12%", BigDecimal("280000")),
            ),
            result.readings,
        )
        assertEquals(listOf("Loan"), result.ignoredWords)
        assertEquals(true, result.hasCurrency)
    }

    @Test
    fun `the most likely reading, times, comes first so it can be preselected`() {
        assertEquals(BigDecimal("30000"), ambiguous("2,50,000 12%").readings.first().value)
    }

    @Test
    fun `ambiguity inside a longer sum keeps the rest of the sum`() {
        val readings = ambiguous("500 + 2,00,000 10%").readings

        assertEquals(BigDecimal("20500"), readings[0].value)
        assertEquals("500 + 2,00,000 × 10%", readings[0].readAs)
        assertEquals(BigDecimal("220550"), readings[1].value)
    }

    @Test
    fun `two plain numbers with no percent stay unreadable, not ambiguous`() {
        assertEquals(CalcResult.Error(ErrorReason.UNREADABLE), Calculator.evaluate("Tea 40 30"))
    }

    @Test
    fun `two separate gaps are unreadable rather than a combinatorial guess`() {
        assertEquals(CalcResult.Error(ErrorReason.UNREADABLE), Calculator.evaluate("100 10% 200 5%"))
    }
}

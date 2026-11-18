package `in`.simply.hisaab.parser

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** Design board rule: a × p% = a × p/100; a + p% and a − p% apply p% of a; a bare p% is p/100. */
class PercentTest {

    private fun success(text: String): CalcResult.Success {
        val result = Calculator.evaluate(text)
        assertIs<CalcResult.Success>(result, "expected success for \"$text\" but got $result")
        return result
    }

    @Test
    fun `multiplying by a percent takes that share`() {
        val result = success("2,50,000 × 12%")
        assertEquals(BigDecimal("30000"), result.value)
        assertEquals("2,50,000 × 12%", result.readAs)
    }

    @Test
    fun `adding a percent adds that share of the left side`() {
        assertEquals(BigDecimal("280000"), success("2,50,000 + 12%").value)
    }

    @Test
    fun `subtracting a percent removes that share of the left side`() {
        assertEquals(BigDecimal("220000"), success("2,50,000 − 12%").value)
        assertEquals(BigDecimal("3499.3"), success("₹4,999 − 30%").value)
    }

    @Test
    fun `a bare percent is a fraction`() {
        assertEquals(BigDecimal("0.12"), success("12%").value)
    }

    @Test
    fun `dividing by a percent divides by the fraction`() {
        assertEquals(BigDecimal("500"), success("50 ÷ 10%").value)
    }

    @Test
    fun `percent inside a product binds to the product, not the running total`() {
        assertEquals(BigDecimal("102"), success("100 + 20 × 10%").value)
    }

    @Test
    fun `a percent after a sum applies to the running total`() {
        assertEquals(BigDecimal("225"), success("200 + 10% + 5").value)
    }

    @Test
    fun `a space before the percent sign is allowed`() {
        assertEquals(BigDecimal("30000"), success("2,50,000 × 12 %").value)
    }
}

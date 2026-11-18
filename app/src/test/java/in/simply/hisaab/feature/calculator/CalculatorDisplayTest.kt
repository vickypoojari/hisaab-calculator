package `in`.simply.hisaab.feature.calculator

import `in`.simply.hisaab.parser.ErrorReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorDisplayTest {

    @Test
    fun `empty shows a zero result and nothing to submit`() {
        val d = CalculatorDisplay.from("")

        assertEquals("", d.expressionText)
        assertEquals("0", d.resultText)
        assertTrue(d.isEmpty)
        assertNull(d.error)
        assertNull(d.wordLine)
        assertFalse(d.equalsEnabled)
    }

    @Test
    fun `the expression is grouped Indian style with spaced operators and the result is live`() {
        val d = CalculatorDisplay.from("120000+85000")

        assertEquals("1,20,000 + 85,000", d.expressionText)
        assertEquals("2,05,000", d.resultText)
        assertEquals("2.05 lakh", d.wordLine)
        assertFalse(d.isEmpty)
        assertTrue(d.equalsEnabled)
    }

    @Test
    fun `a trailing operator shows the running value of what came before it`() {
        val d = CalculatorDisplay.from("1200+")

        assertEquals("1,200 +", d.expressionText)
        assertEquals("1,200", d.resultText)
    }

    @Test
    fun `decimals and a half-typed decimal point are kept as typed`() {
        assertEquals("1,234.5 × 2", CalculatorDisplay.from("1234.5×2").expressionText)
        assertEquals("2,469", CalculatorDisplay.from("1234.5×2").resultText)
        assertEquals("5.", CalculatorDisplay.from("5.").expressionText)
        assertEquals("5", CalculatorDisplay.from("5.").resultText)
    }

    @Test
    fun `a leading minus is shown against its number and carries into the result`() {
        val d = CalculatorDisplay.from("−5+3")

        assertEquals("−5 + 3", d.expressionText)
        assertEquals("−2", d.resultText)
    }

    @Test
    fun `a lone minus is not yet a sum`() {
        val d = CalculatorDisplay.from("−")

        assertEquals("−", d.expressionText)
        assertEquals("0", d.resultText)
        assertFalse(d.isEmpty)
        assertFalse(d.equalsEnabled)
    }

    @Test
    fun `percent is shown as typed and follows the percent rules`() {
        val d = CalculatorDisplay.from("2500+10%")

        assertEquals("2,500 + 10%", d.expressionText)
        assertEquals("2,750", d.resultText)
    }

    @Test
    fun `big results get the crore caption`() {
        val d = CalculatorDisplay.from("1000000×50")

        assertEquals("5,00,00,000", d.resultText)
        assertEquals("5 crore", d.wordLine)
    }

    @Test
    fun `dividing by zero shows the error instead of a result and disables equals`() {
        val d = CalculatorDisplay.from("1200÷0")

        assertEquals("1,200 ÷ 0", d.expressionText)
        assertEquals(ErrorReason.DIVIDE_BY_ZERO, d.error)
        assertEquals("", d.resultText)
        assertNull(d.wordLine)
        assertFalse(d.equalsEnabled)
    }

    @Test
    fun `text that cannot be read shows the unreadable error`() {
        val d = CalculatorDisplay.from("5%3")

        assertEquals(ErrorReason.UNREADABLE, d.error)
        assertFalse(d.equalsEnabled)
    }
}

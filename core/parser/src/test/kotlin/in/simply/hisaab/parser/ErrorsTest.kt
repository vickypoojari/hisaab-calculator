package `in`.simply.hisaab.parser

import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorsTest {

    private fun errorOf(text: String): ErrorReason {
        val result = Calculator.evaluate(text)
        check(result is CalcResult.Error) { "expected Error for \"$text\" but got $result" }
        return result.reason
    }

    @Test
    fun `dividing by zero is a maths error`() {
        assertEquals(ErrorReason.DIVIDE_BY_ZERO, errorOf("Split ₹1,200 ÷ 0"))
        assertEquals(ErrorReason.DIVIDE_BY_ZERO, errorOf("10 / 0.00"))
    }

    @Test
    fun `operators in a row are unreadable`() {
        assertEquals(ErrorReason.UNREADABLE, errorOf("12 + + / 5"))
    }

    @Test
    fun `a trailing or leading operator is unreadable`() {
        assertEquals(ErrorReason.UNREADABLE, errorOf("12 +"))
        assertEquals(ErrorReason.UNREADABLE, errorOf("× 12"))
    }

    @Test
    fun `two numbers with no operator between them are unreadable`() {
        assertEquals(ErrorReason.UNREADABLE, errorOf("Tea 40 30"))
    }

    @Test
    fun `error carries nothing else to misuse`() {
        assertEquals(CalcResult.Error(ErrorReason.UNREADABLE), Calculator.evaluate("12 + +"))
    }
}

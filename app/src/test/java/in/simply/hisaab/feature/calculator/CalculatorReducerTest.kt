package `in`.simply.hisaab.feature.calculator

import `in`.simply.hisaab.feature.calculator.CalculatorKey.BACKSPACE
import `in`.simply.hisaab.feature.calculator.CalculatorKey.CLEAR
import `in`.simply.hisaab.feature.calculator.CalculatorKey.D0
import `in`.simply.hisaab.feature.calculator.CalculatorKey.D1
import `in`.simply.hisaab.feature.calculator.CalculatorKey.D2
import `in`.simply.hisaab.feature.calculator.CalculatorKey.D3
import `in`.simply.hisaab.feature.calculator.CalculatorKey.D4
import `in`.simply.hisaab.feature.calculator.CalculatorKey.D5
import `in`.simply.hisaab.feature.calculator.CalculatorKey.D7
import `in`.simply.hisaab.feature.calculator.CalculatorKey.D9
import `in`.simply.hisaab.feature.calculator.CalculatorKey.DECIMAL
import `in`.simply.hisaab.feature.calculator.CalculatorKey.DIVIDE
import `in`.simply.hisaab.feature.calculator.CalculatorKey.DOUBLE_ZERO
import `in`.simply.hisaab.feature.calculator.CalculatorKey.EQUALS
import `in`.simply.hisaab.feature.calculator.CalculatorKey.MINUS
import `in`.simply.hisaab.feature.calculator.CalculatorKey.MULTIPLY
import `in`.simply.hisaab.feature.calculator.CalculatorKey.PERCENT
import `in`.simply.hisaab.feature.calculator.CalculatorKey.PLUS
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorReducerTest {

    /** Presses the keys in order, starting from an empty expression. */
    private fun typed(vararg keys: CalculatorKey, from: String = ""): String =
        keys.fold(from) { expression, key -> CalculatorReducer.press(expression, key) }

    @Test
    fun `digits append`() {
        assertEquals("78", typed(D7, CalculatorKey.D8))
    }

    @Test
    fun `a leading zero is replaced by the next digit and never doubled`() {
        assertEquals("5", typed(D0, D5))
        assertEquals("0", typed(D0, D0))
        assertEquals("0", typed(DOUBLE_ZERO))
        assertEquals("500", typed(D5, DOUBLE_ZERO))
        assertEquals("5+0", typed(D5, PLUS, DOUBLE_ZERO))
        assertEquals("5+7", typed(D5, PLUS, D0, D7))
    }

    @Test
    fun `decimal point starts a zero and is allowed once per number`() {
        assertEquals("0.", typed(DECIMAL))
        assertEquals("5.", typed(D5, DECIMAL))
        assertEquals("5.", typed(D5, DECIMAL, DECIMAL))
        assertEquals("5.2+0.", typed(D5, DECIMAL, D2, PLUS, DECIMAL))
        assertEquals("5.2+3.", typed(D5, DECIMAL, D2, PLUS, D3, DECIMAL))
    }

    @Test
    fun `an operator needs a number before it, except a leading minus`() {
        assertEquals("", typed(PLUS))
        assertEquals("", typed(MULTIPLY))
        assertEquals("−", typed(MINUS))
        assertEquals("−", typed(MINUS, PLUS))
        assertEquals("−5", typed(MINUS, D5))
        assertEquals("5+", typed(D5, PLUS))
    }

    @Test
    fun `a second operator replaces the first instead of stacking`() {
        assertEquals("5×", typed(D5, PLUS, MULTIPLY))
        assertEquals("5−", typed(D5, MULTIPLY, MINUS))
        assertEquals("5÷", typed(D5, MINUS, DIVIDE))
    }

    @Test
    fun `percent follows a number once, and nothing numeric follows a percent`() {
        assertEquals("5%", typed(D5, PERCENT))
        assertEquals("5%", typed(D5, PERCENT, PERCENT))
        assertEquals("", typed(PERCENT))
        assertEquals("5+", typed(D5, PLUS, PERCENT))
        assertEquals("5%", typed(D5, PERCENT, D3))
        assertEquals("5%+", typed(D5, PERCENT, PLUS))
    }

    @Test
    fun `backspace removes the last character and clear empties everything`() {
        assertEquals("1", typed(D1, D2, BACKSPACE))
        assertEquals("", typed(BACKSPACE))
        assertEquals("", typed(MINUS, BACKSPACE))
        assertEquals("", typed(D1, PLUS, D2, CLEAR))
    }

    @Test
    fun `a number stops growing at 15 digits`() {
        val fifteen = "9".repeat(15)
        assertEquals(fifteen, typed(D9, from = fifteen))
        assertEquals("$fifteen+9", typed(PLUS, D9, from = fifteen))
    }

    @Test
    fun `equals replaces the expression with its result as plain digits`() {
        assertEquals("1650", typed(D1, D2, DOUBLE_ZERO, PLUS, D4, D5, D0, EQUALS))
        assertEquals("0.3333333333", typed(D1, DIVIDE, D3, EQUALS))
    }

    @Test
    fun `a negative result keeps a real minus so the sum can carry on`() {
        val negative = typed(D5, MINUS, D1, D0, EQUALS)
        assertEquals("−5", negative)
        assertEquals("−2", typed(PLUS, D3, EQUALS, from = negative))
    }

    @Test
    fun `equals with a trailing operator uses what came before it`() {
        assertEquals("5", typed(D5, PLUS, EQUALS))
    }

    @Test
    fun `equals leaves an expression that cannot be computed alone`() {
        assertEquals("5÷0", typed(D5, DIVIDE, D0, EQUALS))
        assertEquals("", typed(EQUALS))
    }
}

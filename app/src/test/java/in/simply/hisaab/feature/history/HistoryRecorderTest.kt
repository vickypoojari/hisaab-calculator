package `in`.simply.hisaab.feature.history

import `in`.simply.hisaab.feature.selection.SelectionPresenter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class HistoryRecorderTest {

    private fun fromCalculator(expression: String) = HistoryRecorder.fromCalculator(expression, nowMillis = 42L)

    private fun fromSelection(text: String, index: Int = 0): NewHistoryEntry? {
        var state = SelectionPresenter.present(text, canReplace = true)
        if (state is `in`.simply.hisaab.feature.selection.SelectionUiState.Ambiguous) state = state.select(index)
        return HistoryRecorder.fromSelection(state, nowMillis = 42L)
    }

    @Test
    fun `a calculator sum is recorded as shown, with its plain value`() {
        val e = fromCalculator("1200+450")!!

        assertEquals("1,200 + 450", e.expression)
        assertEquals("1,650", e.result)
        assertEquals("1650", e.value)
        assertEquals(HistorySource.CALCULATOR, e.source)
        assertEquals(42L, e.createdAt)
    }

    @Test
    fun `a negative result keeps its sign in the value`() {
        val e = fromCalculator("−5+3")!!

        assertEquals("−5 + 3", e.expression)
        assertEquals("−2", e.result)
        assertEquals("-2", e.value)
    }

    @Test
    fun `a trailing operator is dropped from what is recorded`() {
        assertEquals("5 + 3", fromCalculator("5+3+")!!.expression)
    }

    @Test
    fun `percent counts as a sum`() {
        assertNotNull(fromCalculator("2500+10%"))
        assertEquals("0.12", fromCalculator("12%")!!.value)
    }

    @Test
    fun `a lone number, nothing, or a failed sum is not worth saving`() {
        assertNull(fromCalculator(""))
        assertNull(fromCalculator("5"))
        assertNull(fromCalculator("5+"))
        assertNull(fromCalculator("−5"))
        assertNull(fromCalculator("1200÷0"))
    }

    @Test
    fun `a selected-text result is recorded with the reading, the rupee sign and its source`() {
        val e = fromSelection("Order Zomato ₹1,200 + ₹450 delivery")!!

        assertEquals("₹1,200 + ₹450", e.expression)
        assertEquals("₹1,650", e.result)
        assertEquals("1650", e.value)
        assertEquals(HistorySource.SELECTED_TEXT, e.source)
    }

    @Test
    fun `an ambiguous selection records the reading that was chosen`() {
        val e = fromSelection("Loan ₹2,50,000 12%", index = 1)!!

        assertEquals("₹2,50,000 + 12%", e.expression)
        assertEquals("₹2,80,000", e.result)
    }

    @Test
    fun `selected text that is not a sum is not recorded`() {
        assertNull(fromSelection("Reaching by evening, call me"))
        assertNull(fromSelection("Split ₹1,200 ÷ 0"))
        assertNull(fromSelection("Total ₹450"))
    }
}

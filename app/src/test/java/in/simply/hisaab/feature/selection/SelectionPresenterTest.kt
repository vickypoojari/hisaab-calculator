package `in`.simply.hisaab.feature.selection

import `in`.simply.hisaab.parser.ErrorReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectionPresenterTest {

    private inline fun <reified T : SelectionUiState> present(text: String, canReplace: Boolean = true): T {
        val state = SelectionPresenter.present(text, canReplace)
        assertTrue("expected ${T::class.simpleName} for \"$text\" but got $state", state is T)
        return state as T
    }

    // A · success, editable field
    @Test
    fun `success in an editable field offers replace and copy`() {
        val s = present<SelectionUiState.Success>("Order Zomato ₹1,200 + ₹450 delivery")

        assertEquals("₹1,200 + ₹450", s.readAs)
        assertEquals(listOf("Order", "Zomato", "delivery"), s.ignoredWords)
        assertEquals("₹1,650", s.resultText)
        assertEquals("1650", s.copyText)
        assertEquals("₹1,650", s.replaceText)
    }

    // B · success, read-only text
    @Test
    fun `read-only text has no replace text`() {
        val s = present<SelectionUiState.Success>("₹4,50,000 − ₹1,50,000 advance", canReplace = false)

        assertEquals("₹3,00,000", s.resultText)
        assertEquals("300000", s.copyText)
        assertNull(s.replaceText)
    }

    @Test
    fun `replace keeps rupee and grouping only if the selected text used them`() {
        assertEquals("₹500", present<SelectionUiState.Success>("₹300 + ₹200").replaceText)
        assertEquals("2,05,000", present<SelectionUiState.Success>("1,20,000 + 85,000").replaceText)
        assertEquals("6200", present<SelectionUiState.Success>("5000+1200").replaceText)
    }

    @Test
    fun `the displayed result is always grouped Indian style, copy is always plain digits`() {
        val s = present<SelectionUiState.Success>("5000+1200")

        assertEquals("6,200", s.resultText)
        assertEquals("6200", s.copyText)
    }

    @Test
    fun `negative results show a real minus sign`() {
        assertEquals("−5", present<SelectionUiState.Success>("10 − 15").resultText)
    }

    // C · ambiguous
    @Test
    fun `ambiguous text lists every reading and preselects the most likely`() {
        val s = present<SelectionUiState.Ambiguous>("Loan ₹2,50,000 12%")

        assertEquals(2, s.readings.size)
        assertEquals(0, s.selectedIndex)
        assertEquals("₹2,50,000 × 12%", s.selected.readAs)
        assertEquals("₹30,000", s.selected.resultText)
        assertEquals("₹30,000", s.selected.replaceText)
        assertEquals(listOf("Loan"), s.ignoredWords)
    }

    @Test
    fun `choosing another reading updates the result, copy and replace text`() {
        val s = present<SelectionUiState.Ambiguous>("Loan ₹2,50,000 12%").select(1)

        assertEquals("₹2,80,000", s.selected.resultText)
        assertEquals("280000", s.selected.copyText)
        assertEquals("₹2,80,000", s.selected.replaceText)
    }

    @Test
    fun `ambiguous text in a read-only field has no replace text on any reading`() {
        val s = present<SelectionUiState.Ambiguous>("Loan ₹2,50,000 12%", canReplace = false)

        assertTrue(s.readings.all { it.replaceText == null })
    }

    // D · nothing to calculate
    @Test
    fun `text without numbers quotes what was selected`() {
        val s = present<SelectionUiState.NothingToCalculate>("Reaching by evening, call me")

        assertEquals("Reaching by evening, call me", s.selectedText)
    }

    // E · parse error
    @Test
    fun `divide by zero is a maths error that keeps the selected text`() {
        val s = present<SelectionUiState.Error>("Split ₹1,200 ÷ 0")

        assertEquals(ErrorReason.DIVIDE_BY_ZERO, s.reason)
        assertEquals("Split ₹1,200 ÷ 0", s.selectedText)
    }

    @Test
    fun `unreadable text is an error too`() {
        assertEquals(ErrorReason.UNREADABLE, present<SelectionUiState.Error>("12 + + / 5").reason)
    }
}

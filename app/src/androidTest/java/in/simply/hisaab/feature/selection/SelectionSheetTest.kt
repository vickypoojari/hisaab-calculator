package `in`.simply.hisaab.feature.selection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import `in`.simply.hisaab.core.designsystem.HisaabTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SelectionSheetTest {

    @get:Rule
    val compose = createComposeRule()

    private var replaced: String? = null
    private var copied: String? = null
    private var closed = false
    private var openedApp = false

    private fun show(text: String, canReplace: Boolean = true) {
        var state by mutableStateOf(SelectionPresenter.present(text, canReplace))
        val actions = SelectionActions(
            onReplace = { replaced = it },
            onCopy = { copied = it },
            onSelectReading = { index -> (state as? SelectionUiState.Ambiguous)?.let { state = it.select(index) } },
            onOpenApp = { openedApp = true },
            onClose = { closed = true },
        )
        compose.setContent { HisaabTheme { SelectionSheet(state, actions) } }
    }

    // A
    @Test
    fun success_shows_reading_ignored_words_result_and_both_actions() {
        show("Order Zomato ₹1,200 + ₹450 delivery")

        compose.onNodeWithText("Calculate").assertIsDisplayed()
        compose.onNodeWithText("Read as").assertIsDisplayed()
        compose.onNodeWithText("₹1,200 + ₹450").assertIsDisplayed()
        compose.onNodeWithText("Ignored words: Order, Zomato, delivery").assertIsDisplayed()
        compose.onNodeWithText("= ₹1,650").assertIsDisplayed()
        compose.onNodeWithText("Replace with result").assertIsDisplayed()
        compose.onNodeWithText("Copy").assertIsDisplayed()
        compose.onNodeWithText("Calculated on your phone. Nothing is sent anywhere.").assertIsDisplayed()
    }

    @Test
    fun replace_hands_back_the_replace_text_and_copy_hands_back_plain_digits() {
        show("Order Zomato ₹1,200 + ₹450 delivery")

        compose.onNodeWithText("Replace with result").performClick()
        assertEquals("₹1,650", replaced)

        compose.onNodeWithText("Copy").performClick()
        assertEquals("1650", copied)
    }

    // B
    @Test
    fun read_only_hides_replace_and_says_why() {
        show("₹4,50,000 − ₹1,50,000 advance", canReplace = false)

        compose.onNodeWithText("Replace with result").assertDoesNotExist()
        compose.onNodeWithText("Copy").assertIsDisplayed()
        compose.onNodeWithText("This text can’t be edited, so Replace isn’t shown.").assertIsDisplayed()
    }

    // C
    @Test
    fun ambiguous_lists_readings_and_updates_the_button_when_one_is_picked() {
        show("Loan ₹2,50,000 12%")

        compose.onNodeWithText("Check this reading").assertIsDisplayed()
        compose.onNodeWithText("₹2,50,000 × 12%").assertIsDisplayed()
        compose.onNodeWithText("₹2,50,000 + 12%").assertIsDisplayed()
        compose.onNodeWithText("Replace with ₹30,000").assertIsDisplayed()

        compose.onNodeWithText("₹2,50,000 + 12%").performClick()

        compose.onNodeWithText("Replace with ₹2,80,000").assertIsDisplayed()
        compose.onNodeWithText("Replace with ₹2,80,000").performClick()
        assertEquals("₹2,80,000", replaced)
    }

    // D
    @Test
    fun nothing_to_calculate_explains_quotes_the_text_and_offers_a_way_on() {
        show("Reaching by evening, call me")

        compose.onNodeWithText("No numbers to calculate").assertIsDisplayed()
        compose.onNodeWithText("You selected “Reaching by evening, call me”").assertIsDisplayed()
        compose.onNodeWithText("Copy").assertDoesNotExist()
        compose.onNodeWithText("Replace with result").assertDoesNotExist()

        compose.onNodeWithText("Open Hisaab").performClick()
        assertEquals(true, openedApp)
    }

    // E
    @Test
    fun divide_by_zero_shows_the_reason_and_hides_copy_and_replace() {
        show("Split ₹1,200 ÷ 0")

        compose.onNodeWithText("Can’t divide by zero").assertIsDisplayed()
        compose.onNodeWithText("Copy").assertDoesNotExist()
        compose.onNodeWithText("Replace with result").assertDoesNotExist()
    }

    @Test
    fun unreadable_text_shows_its_own_reason() {
        show("12 + + / 5")

        compose.onNodeWithText("Can’t read this").assertIsDisplayed()
    }

    @Test
    fun close_is_always_available() {
        show("₹1,200 + ₹450")

        // The header close is an icon button, found by its accessibility label.
        compose.onNodeWithContentDescription("Close").performClick()
        assertEquals(true, closed)
    }
}

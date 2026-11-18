package `in`.simply.hisaab.feature.history

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import `in`.simply.hisaab.core.designsystem.HisaabTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HistoryScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val typed = HistoryEntry(1, "1,20,000 + 85,000", "2,05,000", "205000", HistorySource.CALCULATOR, 1)
    private val selected = HistoryEntry(2, "₹1,200 + ₹450", "₹1,650", "1650", HistorySource.SELECTED_TEXT, 2)
    private val older = HistoryEntry(3, "1,299 × 3", "3,897", "3897", HistorySource.CALCULATOR, 3)

    private var reused: HistoryEntry? = null
    private var copied: HistoryEntry? = null
    private var deleted: HistoryEntry? = null
    private var cleared = false
    private var wentBack = false

    private fun show(sections: List<HistorySection>) {
        val actions = HistoryActions(
            onBack = { wentBack = true },
            onReuse = { reused = it },
            onCopy = { copied = it },
            onDelete = { deleted = it },
            onClearAll = { cleared = true },
        )
        compose.setContent { HisaabTheme { HistoryScreen(sections, actions) } }
    }

    private fun populated() = show(
        listOf(
            HistorySection(DayLabel.Today, listOf(selected, typed)),
            HistorySection(DayLabel.Yesterday, listOf(older)),
        ),
    )

    @Test
    fun shows_a_title_back_clear_all_day_headers_and_every_row() {
        populated()

        compose.onNodeWithText("History").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").assertIsDisplayed()
        compose.onNodeWithText("Clear all").assertIsDisplayed()
        compose.onNodeWithText("TODAY").assertIsDisplayed()
        compose.onNodeWithText("YESTERDAY").assertIsDisplayed()
        listOf(typed, selected, older).forEach {
            compose.onNodeWithText(it.expression).assertIsDisplayed()
            compose.onNodeWithText(it.result).assertIsDisplayed()
        }
    }

    @Test
    fun only_rows_from_selected_text_carry_the_caption() {
        populated()

        // Exactly one entry came from selected text, so exactly one caption.
        compose.onNodeWithText("From selected text").assertIsDisplayed()
        assertEquals(1, compose.onAllNodesWithTextCount("From selected text"))
    }

    @Test
    fun rows_are_at_least_72dp_tall() {
        populated()

        compose.onNodeWithText(typed.expression).assertHeightIsAtLeast(72.dp)
    }

    @Test
    fun tapping_a_row_reuses_its_result() {
        populated()

        compose.onNodeWithText(typed.expression).performClick()

        assertEquals(typed, reused)
    }

    @Test
    fun long_press_offers_copy_and_delete() {
        populated()

        compose.onNodeWithText(typed.expression).performTouchInput { longClick() }
        compose.onNodeWithText("Copy").performClick()
        assertEquals(typed, copied)

        compose.onNodeWithText(selected.expression).performTouchInput { longClick() }
        compose.onNodeWithText("Delete").performClick()
        assertEquals(selected, deleted)
    }

    @Test
    fun clear_all_asks_first_and_cancel_changes_nothing() {
        populated()

        compose.onNodeWithText("Clear all").performClick()
        compose.onNodeWithText("Clear all history?").assertIsDisplayed()
        compose.onNodeWithText("Cancel").performClick()

        assertTrue(!cleared)
        compose.onNodeWithText("Clear all history?").assertDoesNotExist()
    }

    @Test
    fun confirming_clear_all_clears() {
        populated()

        compose.onNodeWithText("Clear all").performClick()
        compose.onNodeWithText("Clear").performClick()

        assertTrue(cleared)
    }

    @Test
    fun empty_history_says_so_and_has_nothing_to_clear() {
        show(emptyList())

        compose.onNodeWithText("No sums yet. Your answers will show up here.").assertIsDisplayed()
        compose.onNodeWithText("Clear all").assertDoesNotExist()
    }

    @Test
    fun back_goes_back() {
        populated()

        compose.onNodeWithContentDescription("Back").performClick()

        assertTrue(wentBack)
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithTextCount(text: String): Int =
        onAllNodesWithText(text).fetchSemanticsNodes().size
}

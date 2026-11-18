package `in`.simply.hisaab.feature.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.platform.app.InstrumentationRegistry
import `in`.simply.hisaab.core.designsystem.HisaabTheme
import `in`.simply.hisaab.feature.calculator.CalculatorKey
import `in`.simply.hisaab.feature.calculator.CalculatorKey.*
import `in`.simply.hisaab.feature.calculator.CalculatorRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** The calculator and the History screen working together, as a person would use them. */
class HistoryFlowTest {

    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var history: HistoryRepository

    private fun show() {
        context.deleteDatabase("flow-test.db")
        history = HistoryRepository(SqliteHistoryStore(context, "flow-test.db"))
        compose.setContent { HisaabTheme { CalculatorRoute(history) } }
    }

    private fun press(vararg keys: CalculatorKey) = keys.forEach { compose.onNodeWithContentDescription(it.spoken).performClick() }

    private fun openHistory() = compose.onNodeWithContentDescription("History").performClick()

    private fun typeAndCommit1200Plus450() = press(D1, D2, DOUBLE_ZERO, PLUS, D4, D5, D0, EQUALS)

    @Test
    fun equals_saves_the_sum_and_history_shows_it_under_today() {
        show()

        typeAndCommit1200Plus450()
        assertEquals(listOf("1,200 + 450"), history.entries.value.map { it.expression })

        openHistory()
        compose.onNodeWithText("TODAY").assertIsDisplayed()
        compose.onNodeWithText("1,200 + 450").assertIsDisplayed()
        compose.onNodeWithText("1,650").assertIsDisplayed()
    }

    @Test
    fun a_lone_number_is_not_saved() {
        show()

        press(D5, EQUALS)

        assertTrue(history.entries.value.isEmpty())
    }

    @Test
    fun history_opens_empty_with_a_message() {
        show()

        openHistory()

        compose.onNodeWithText("No sums yet. Your answers will show up here.").assertIsDisplayed()
    }

    @Test
    fun tapping_a_row_brings_its_result_back_into_the_calculator() {
        show()
        typeAndCommit1200Plus450()
        press(CLEAR)

        openHistory()
        compose.onNodeWithText("1,200 + 450").performClick()

        compose.onNodeWithTag("expression").assertTextEquals("1,650")
        compose.onNodeWithText("No sums yet. Your answers will show up here.").assertDoesNotExist()
    }

    @Test
    fun back_returns_to_the_calculator_with_the_expression_untouched() {
        show()
        press(D7, PLUS, D3)

        openHistory()
        compose.onNodeWithContentDescription("Back").performClick()

        compose.onNodeWithTag("expression").assertTextEquals("7 + 3")
    }

    @Test
    fun long_press_delete_removes_the_row_everywhere() {
        show()
        typeAndCommit1200Plus450()
        openHistory()

        compose.onNodeWithText("1,200 + 450").performTouchInput { longClick() }
        compose.onNodeWithText("Delete").performClick()

        compose.onNodeWithText("No sums yet. Your answers will show up here.").assertIsDisplayed()
        assertTrue(history.entries.value.isEmpty())
    }

    @Test
    fun clear_all_needs_a_confirmation_and_then_empties_everything() {
        show()
        typeAndCommit1200Plus450()
        openHistory()

        compose.onNodeWithText("Clear all").performClick()
        compose.onNodeWithText("Clear").performClick()

        compose.onNodeWithText("No sums yet. Your answers will show up here.").assertIsDisplayed()
        assertTrue(history.entries.value.isEmpty())
    }
}

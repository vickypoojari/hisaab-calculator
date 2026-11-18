package `in`.simply.hisaab.feature.calculator

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import `in`.simply.hisaab.core.designsystem.HisaabTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CalculatorScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private fun show() = compose.setContent { HisaabTheme { CalculatorRoute() } }

    private fun press(vararg keys: CalculatorKey) = keys.forEach {
        compose.onNodeWithContentDescription(it.spoken).performClick()
    }

    @Test
    fun empty_state_shows_the_name_a_prompt_a_zero_result_and_all_twenty_keys() {
        show()

        compose.onNodeWithText("Hisaab").assertIsDisplayed()
        compose.onNodeWithText("Type numbers").assertIsDisplayed()
        compose.onNodeWithTag("result").assertTextEquals("0")
        CalculatorKey.entries.forEach { compose.onNodeWithContentDescription(it.spoken).assertIsDisplayed() }
        assertEquals(20, CalculatorKey.entries.size)
    }

    @Test
    fun typing_shows_a_grouped_expression_a_live_result_and_the_word_line() {
        show()

        press(
            CalculatorKey.D1, CalculatorKey.D2, CalculatorKey.DOUBLE_ZERO, CalculatorKey.DOUBLE_ZERO,
            CalculatorKey.PLUS, CalculatorKey.D8, CalculatorKey.D5,
            CalculatorKey.DOUBLE_ZERO, CalculatorKey.D0,
        )

        compose.onNodeWithTag("expression").assertTextEquals("1,20,000 + 85,000")
        compose.onNodeWithTag("result").assertTextEquals("2,05,000")
        compose.onNodeWithTag("wordLine").assertTextEquals("2.05 lakh")
    }

    @Test
    fun equals_moves_the_result_into_the_expression() {
        show()

        press(CalculatorKey.D1, CalculatorKey.D2, CalculatorKey.DOUBLE_ZERO, CalculatorKey.PLUS,
            CalculatorKey.D4, CalculatorKey.D5, CalculatorKey.D0, CalculatorKey.EQUALS)

        // The committed result is now the expression and is also the live result.
        compose.onNodeWithTag("expression").assertTextEquals("1,650")
        compose.onNodeWithTag("result").assertTextEquals("1,650")
    }

    @Test
    fun dividing_by_zero_shows_the_reason_and_disables_equals() {
        show()

        press(CalculatorKey.D1, CalculatorKey.D2, CalculatorKey.DOUBLE_ZERO, CalculatorKey.DIVIDE, CalculatorKey.D0)

        compose.onNodeWithText("Can’t divide by zero").assertIsDisplayed()
        compose.onNodeWithContentDescription("Equals").assertIsNotEnabled()
    }

    @Test
    fun equals_is_enabled_once_there_is_a_result() {
        show()

        press(CalculatorKey.D5, CalculatorKey.PLUS, CalculatorKey.D3)

        compose.onNodeWithContentDescription("Equals").assertIsEnabled()
    }

    @Test
    fun backspace_and_clear_work() {
        show()

        press(CalculatorKey.D1, CalculatorKey.D2, CalculatorKey.BACKSPACE)
        compose.onNodeWithTag("expression").assertTextEquals("1")

        press(CalculatorKey.CLEAR)
        compose.onNodeWithText("Type numbers").assertIsDisplayed()
    }

    @Test
    fun keys_meet_the_design_minimum_size() {
        show()

        CalculatorKey.entries.forEach {
            compose.onNodeWithContentDescription(it.spoken).assertWidthIsAtLeast(72.dp).assertHeightIsAtLeast(64.dp)
        }
    }
}

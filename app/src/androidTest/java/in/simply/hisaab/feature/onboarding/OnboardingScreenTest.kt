package `in`.simply.hisaab.feature.onboarding

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import `in`.simply.hisaab.core.designsystem.HisaabTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OnboardingScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private var finished = 0

    private fun show() = compose.setContent { HisaabTheme { OnboardingScreen(onFinish = { finished++ }) } }

    @Test
    fun step_one_shows_where_calculate_hides() {
        show()

        compose.onNodeWithText("Step 1 of 2").assertIsDisplayed()
        compose.onNodeWithText("Skip").assertIsDisplayed()
        compose.onNodeWithContentDescription(
            "Illustration: select some text, tap the three dots, then tap Calculate in the menu.",
        ).assertIsDisplayed()
        compose.onNodeWithText("Calculate inside any app").assertIsDisplayed()
        compose.onNodeWithText(
            "Select text that has numbers. Tap the three dots, then tap Calculate. On some phones the dots say “More”.",
        ).assertIsDisplayed()
        compose.onNodeWithText("Next").assertIsDisplayed()
    }

    @Test
    fun next_opens_the_practice_screen() {
        show()

        compose.onNodeWithText("Next").performClick()

        compose.onNodeWithText("Step 2 of 2").assertIsDisplayed()
        compose.onNodeWithText("Try it on this text").assertIsDisplayed()
        compose.onNodeWithText("Tea ₹40 + samosa ₹30 × 2").assertIsDisplayed()
        compose.onNodeWithText("Your answer appears in a small box at the bottom. Nothing leaves your phone.").assertIsDisplayed()
        compose.onNodeWithText("Done").assertIsDisplayed()
        compose.onNodeWithText("Back").assertIsDisplayed()
    }

    @Test
    fun back_returns_to_step_one() {
        show()
        compose.onNodeWithText("Next").performClick()

        compose.onNodeWithText("Back").performClick()

        compose.onNodeWithText("Step 1 of 2").assertIsDisplayed()
        assertEquals(0, finished)
    }

    @Test
    fun skip_finishes_from_either_screen() {
        show()
        compose.onNodeWithText("Skip").performClick()
        assertEquals(1, finished)
    }

    @Test
    fun skip_on_the_practice_screen_finishes_too() {
        show()
        compose.onNodeWithText("Next").performClick()

        compose.onNodeWithText("Skip").performClick()

        assertEquals(1, finished)
    }

    @Test
    fun done_finishes() {
        show()
        compose.onNodeWithText("Next").performClick()

        compose.onNodeWithText("Done").performClick()

        assertEquals(1, finished)
    }

    @Test
    fun buttons_meet_the_design_sizes() {
        show()

        compose.onNodeWithText("Next").assertHeightIsAtLeast(56.dp)
        compose.onNodeWithText("Skip").assertHeightIsAtLeast(48.dp)
    }
}

package `in`.simply.hisaab.feature.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.unit.Density
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

    // --- found by looking at the real app: layout problems, not logic ---

    /** A small screen with the largest text size: the case where buttons used to scroll away. */
    private fun showHugeTextOnASmallScreen() = compose.setContent {
        val base = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale = 2f)) {
            HisaabTheme { Box(Modifier.size(width = 360.dp, height = 640.dp)) { OnboardingScreen(onFinish = { finished++ }) } }
        }
    }

    @Test
    fun skip_and_next_stay_reachable_with_huge_text_on_a_small_screen() {
        showHugeTextOnASmallScreen()

        compose.onNodeWithText("Skip").assertIsDisplayed()
        compose.onNodeWithText("Next").assertIsDisplayed()
        compose.onNodeWithText("Next").performClick()

        compose.onNodeWithText("Done").assertIsDisplayed()
        compose.onNodeWithText("Back").assertIsDisplayed()
    }

    /** The design's phone: 360 x 740 dp at normal text size. */
    private fun showOnAPhone() = compose.setContent {
        CompositionLocalProvider(LocalDensity provides Density(2.625f, fontScale = 1f)) {
            HisaabTheme { Box(Modifier.size(width = 360.dp, height = 740.dp)) { OnboardingScreen(onFinish = { finished++ }) } }
        }
    }

    @Test
    fun the_demo_toolbar_labels_stay_on_one_line() {
        showOnAPhone()

        listOf("Cut", "Copy", "Paste").forEach { label ->
            val layouts = mutableListOf<TextLayoutResult>()
            compose.onNodeWithText(label, useUnmergedTree = true).fetchSemanticsNode()
                .config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
            assertEquals("$label wraps onto several lines", 1, layouts.single().lineCount)
        }
    }

    @Test
    fun the_demo_shows_both_numbered_steps() {
        showOnAPhone()

        compose.onNodeWithText("1", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText("2", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun the_demo_line_keeps_the_space_before_the_selected_part() {
        show()

        val text = compose.onNodeWithText("₹320 + tip ₹40", substring = true, useUnmergedTree = true)
            .fetchSemanticsNode().config[SemanticsProperties.Text].first().text

        assertEquals("Lunch ₹320 + tip ₹40", text)
    }
}

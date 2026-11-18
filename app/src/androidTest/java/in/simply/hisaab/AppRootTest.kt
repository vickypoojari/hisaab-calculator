package `in`.simply.hisaab

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import `in`.simply.hisaab.core.designsystem.HisaabTheme
import `in`.simply.hisaab.feature.calculator.CalculatorKey
import `in`.simply.hisaab.feature.history.HistoryRepository
import `in`.simply.hisaab.feature.history.SqliteHistoryStore
import `in`.simply.hisaab.feature.onboarding.HintChip
import `in`.simply.hisaab.feature.onboarding.OnboardingProgress
import `in`.simply.hisaab.feature.onboarding.Preferences
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AppRootTest {

    @get:Rule
    val compose = createComposeRule()

    private class FakePreferences : Preferences {
        private val values = mutableMapOf<String, Any>()
        override fun getBoolean(key: String, default: Boolean) = values[key] as? Boolean ?: default
        override fun putBoolean(key: String, value: Boolean) { values[key] = value }
        override fun getInt(key: String, default: Int) = values[key] as? Int ?: default
        override fun putInt(key: String, value: Int) { values[key] = value }
    }

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefs = FakePreferences()

    /** [firstLaunch] false means onboarding was already seen. */
    private fun show(firstLaunch: Boolean = false) {
        context.deleteDatabase("root-test.db")
        val history = HistoryRepository(SqliteHistoryStore(context, "root-test.db"))
        val progress = OnboardingProgress(prefs)
        if (!firstLaunch) progress.markDone()
        compose.setContent { HisaabTheme { AppRoot(history, progress, HintChip(prefs)) } }
    }

    private fun press(vararg keys: CalculatorKey) = keys.forEach { compose.onNodeWithContentDescription(it.spoken).performClick() }

    // --- first launch ---

    @Test
    fun a_first_launch_opens_onboarding_and_skip_leads_to_the_calculator_for_good() {
        show(firstLaunch = true)

        compose.onNodeWithText("Step 1 of 2").assertIsDisplayed()
        compose.onNodeWithText("Skip").performClick()

        compose.onNodeWithText("Type numbers").assertIsDisplayed()
        assertTrue(OnboardingProgress(prefs).isDone)
    }

    @Test
    fun finishing_the_practice_screen_also_counts_as_seen() {
        show(firstLaunch = true)

        compose.onNodeWithText("Next").performClick()
        compose.onNodeWithText("Done").performClick()

        compose.onNodeWithText("Type numbers").assertIsDisplayed()
        assertTrue(OnboardingProgress(prefs).isDone)
    }

    @Test
    fun a_later_launch_goes_straight_to_the_calculator() {
        show(firstLaunch = false)

        compose.onNodeWithText("Type numbers").assertIsDisplayed()
        compose.onNodeWithText("Step 1 of 2").assertDoesNotExist()
    }

    // --- hint chip ---

    @Test
    fun the_empty_calculator_shows_the_hint_chip_and_dismissing_it_hides_it_for_good() {
        show()

        compose.onNodeWithText("Calculate from any app: see how").assertIsDisplayed()
        compose.onNodeWithContentDescription("Dismiss hint").performClick()

        compose.onNodeWithText("Calculate from any app: see how").assertDoesNotExist()
        assertFalse(HintChip(prefs).isVisibleThisLaunch())
    }

    @Test
    fun the_chip_goes_away_once_there_is_something_typed() {
        show()

        press(CalculatorKey.D5)

        compose.onNodeWithText("Calculate from any app: see how").assertDoesNotExist()
    }

    @Test
    fun tapping_the_chip_opens_the_how_to_and_done_returns_to_the_calculator() {
        show()

        compose.onNodeWithText("Calculate from any app: see how").performClick()
        compose.onNodeWithText("Step 1 of 2").assertIsDisplayed()
        compose.onNodeWithText("Skip").performClick()

        compose.onNodeWithText("Type numbers").assertIsDisplayed()
        compose.onNodeWithText("Calculate from any app: see how").assertDoesNotExist()
    }

    // --- More menu ---

    @Test
    fun the_more_menu_offers_how_to_use_and_about_and_nothing_that_does_not_work_yet() {
        show()

        compose.onNodeWithContentDescription("More options").performClick()

        compose.onNodeWithText("How to use Calculate").assertIsDisplayed()
        compose.onNodeWithText("About").assertIsDisplayed()
        compose.onNodeWithText("Settings").assertDoesNotExist()
    }

    @Test
    fun how_to_use_reopens_onboarding_and_leaves_what_was_typed_alone() {
        show()
        press(CalculatorKey.D7, CalculatorKey.PLUS, CalculatorKey.D3)

        compose.onNodeWithContentDescription("More options").performClick()
        compose.onNodeWithText("How to use Calculate").performClick()
        compose.onNodeWithText("Step 1 of 2").assertIsDisplayed()
        compose.onNodeWithText("Skip").performClick()

        compose.onNodeWithTag("expression").assertTextEquals("7 + 3")
    }

    @Test
    fun about_says_what_the_app_is_and_that_nothing_leaves_the_phone() {
        show()

        compose.onNodeWithContentDescription("More options").performClick()
        compose.onNodeWithText("About").performClick()

        compose.onNodeWithText("Calculated on your phone. Nothing is sent anywhere.").assertIsDisplayed()
        compose.onNodeWithText("Close").performClick()
        compose.onNodeWithText("Calculated on your phone. Nothing is sent anywhere.").assertDoesNotExist()
    }
}

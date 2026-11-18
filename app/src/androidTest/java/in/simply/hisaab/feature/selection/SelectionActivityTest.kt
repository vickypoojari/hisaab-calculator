package `in`.simply.hisaab.feature.selection

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** The real entry point: what Android sends when someone taps Calculate in the selection menu. */
class SelectionActivityTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private fun processText(text: String?, readOnly: Boolean = false): Intent {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        return Intent(Intent.ACTION_PROCESS_TEXT).apply {
            setClass(context, SelectionActivity::class.java)
            type = "text/plain"
            if (text != null) putExtra(Intent.EXTRA_PROCESS_TEXT, text)
            putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, readOnly)
        }
    }

    @Test
    fun replace_returns_the_result_text_to_the_host_app() {
        ActivityScenario.launchActivityForResult<SelectionActivity>(
            processText("Order Zomato ₹1,200 + ₹450 delivery"),
        ).use { scenario ->
            compose.onNodeWithText("= ₹1,650").assertExists()
            compose.onNodeWithText("Replace with result").performClick()

            assertEquals(Activity.RESULT_OK, scenario.result.resultCode)
            assertEquals("₹1,650", scenario.result.resultData.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString())
        }
    }

    @Test
    fun a_read_only_host_never_sees_replace() {
        ActivityScenario.launchActivityForResult<SelectionActivity>(
            processText("₹4,50,000 − ₹1,50,000 advance", readOnly = true),
        ).use {
            compose.onNodeWithText("= ₹3,00,000").assertExists()
            compose.onNodeWithText("Replace with result").assertDoesNotExist()
            compose.onNodeWithText("Copy").assertExists()
        }
    }

    @Test
    fun closing_cancels_and_hands_nothing_back() {
        ActivityScenario.launchActivityForResult<SelectionActivity>(processText("₹1,200 + ₹450")).use { scenario ->
            compose.onNodeWithContentDescription("Close").performClick()

            assertEquals(Activity.RESULT_CANCELED, scenario.result.resultCode)
            assertNull(scenario.result.resultData?.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT))
        }
    }

    @Test
    fun copy_closes_the_sheet_without_changing_the_host_text() {
        ActivityScenario.launchActivityForResult<SelectionActivity>(processText("₹1,200 + ₹450")).use { scenario ->
            compose.onNodeWithText("Copy").performClick()

            assertEquals(Activity.RESULT_CANCELED, scenario.result.resultCode)
        }
    }

    @Test
    fun a_missing_selection_shows_nothing_to_calculate_instead_of_crashing() {
        ActivityScenario.launchActivityForResult<SelectionActivity>(processText(null)).use {
            compose.onNodeWithText("No numbers to calculate").assertExists()
        }
    }
}

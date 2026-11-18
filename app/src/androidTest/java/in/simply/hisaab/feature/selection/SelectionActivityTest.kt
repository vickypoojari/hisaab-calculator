package `in`.simply.hisaab.feature.selection

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import `in`.simply.hisaab.HisaabApp
import `in`.simply.hisaab.feature.history.HistoryRepository
import `in`.simply.hisaab.feature.history.HistorySource
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** The real entry point: what Android sends when someone taps Calculate in the selection menu. */
class SelectionActivityTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private fun appHistory(): HistoryRepository =
        (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as HisaabApp).history

    @Before
    fun clearHistory() = appHistory().clear()

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
    fun replace_also_puts_the_same_text_on_the_clipboard_for_hosts_that_drop_it() {
        // Some hosts (Chrome) collapse their selection and ignore the result, so Replace must
        // leave the answer on the clipboard as a fallback: one paste instead of retyping.
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        val changed = CountDownLatch(1)
        val listener = ClipboardManager.OnPrimaryClipChangedListener { changed.countDown() }
        InstrumentationRegistry.getInstrumentation().runOnMainSync { clipboard.addPrimaryClipChangedListener(listener) }
        try {
            ActivityScenario.launchActivityForResult<SelectionActivity>(
                processText("Order Zomato ₹1,200 + ₹450 delivery"),
            ).use { scenario ->
                compose.onNodeWithText("Replace with result").performClick()
                scenario.result // wait until the activity has finished

                assertTrue("clipboard was not written", changed.await(5, TimeUnit.SECONDS))
                assertEquals("₹1,650", readClipboardInBackground(clipboard))
            }
        } finally {
            InstrumentationRegistry.getInstrumentation().runOnMainSync { clipboard.removePrimaryClipChangedListener(listener) }
        }
    }

    /** Android blocks background clipboard reads for apps; the shell identity is allowed to. */
    private fun readClipboardInBackground(clipboard: ClipboardManager): String? {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        automation.adoptShellPermissionIdentity("android.permission.READ_CLIPBOARD_IN_BACKGROUND")
        try {
            return clipboard.primaryClip?.getItemAt(0)?.text?.toString()
        } finally {
            automation.dropShellPermissionIdentity()
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

    // --- History: successful results are kept, captioned "From selected text" ---

    @Test
    fun a_successful_result_is_kept_in_history_even_if_the_sheet_is_just_closed() {
        ActivityScenario.launchActivityForResult<SelectionActivity>(
            processText("Order Zomato ₹1,200 + ₹450 delivery"),
        ).use {
            compose.onNodeWithContentDescription("Close").performClick()
        }

        val saved = appHistory().entries.value.single()
        assertEquals("₹1,200 + ₹450", saved.expression)
        assertEquals("₹1,650", saved.result)
        assertEquals(HistorySource.SELECTED_TEXT, saved.source)
    }

    @Test
    fun an_ambiguous_result_is_kept_as_the_reading_that_was_chosen() {
        ActivityScenario.launchActivityForResult<SelectionActivity>(processText("Loan ₹2,50,000 12%")).use {
            compose.onNodeWithText("₹2,50,000 + 12%").performClick()
            compose.onNodeWithText("Replace with ₹2,80,000").performClick()
        }

        val saved = appHistory().entries.value.single()
        assertEquals("₹2,50,000 + 12%", saved.expression)
        assertEquals("₹2,80,000", saved.result)
    }

    @Test
    fun text_that_is_not_a_sum_is_not_kept() {
        listOf("Reaching by evening, call me", "Split ₹1,200 ÷ 0", "Total ₹450").forEach { text ->
            ActivityScenario.launchActivityForResult<SelectionActivity>(processText(text)).use {
                compose.onNodeWithContentDescription("Close").performClick()
            }
        }

        assertTrue(appHistory().entries.value.isEmpty())
    }
}

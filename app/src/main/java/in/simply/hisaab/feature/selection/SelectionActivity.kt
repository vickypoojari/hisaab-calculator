package `in`.simply.hisaab.feature.selection

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import `in`.simply.hisaab.HisaabApp
import `in`.simply.hisaab.MainActivity
import `in`.simply.hisaab.feature.history.HistoryRecorder
import `in`.simply.hisaab.R
import `in`.simply.hisaab.core.designsystem.HisaabTheme

/**
 * Entry point for the "Calculate" action in the text-selection toolbar (ACTION_PROCESS_TEXT).
 * It floats over the host app; Replace hands the answer back through the activity result.
 */
class SelectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val selectedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString().orEmpty()
        val canReplace = !intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false)

        val opened = SelectionPresenter.present(selectedText, canReplace)
        // A clear result is kept in History even if the sheet is just closed.
        if (savedInstanceState == null && opened is SelectionUiState.Success) record(opened)

        setContent {
            HisaabTheme {
                val base = remember(selectedText, canReplace) { opened }
                var chosen by rememberSaveable { mutableIntStateOf(0) }
                val state = if (base is SelectionUiState.Ambiguous) base.select(chosen) else base

                // 32% scrim; tapping outside the sheet closes it.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.32f))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { finish() },
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    SelectionSheet(
                        state = state,
                        actions = SelectionActions(
                            onReplace = { text -> record(state); replaceAndClose(text) },
                            onCopy = { text -> record(state); copyAndClose(text) },
                            onSelectReading = { chosen = it },
                            onOpenApp = ::openAppAndClose,
                            onClose = { finish() },
                        ),
                        modifier = Modifier
                            .navigationBarsPadding()
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
                    )
                }
            }
        }
    }

    /**
     * Hands the answer back to the host. Some hosts (Chrome) collapse their selection when this
     * sheet opens and then ignore the result, so the same text also goes on the clipboard: a
     * failed Replace costs one paste instead of retyping.
     */
    private fun replaceAndClose(text: String) {
        putOnClipboard(text)
        Toast.makeText(applicationContext, R.string.selection_also_copied, Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK, Intent().putExtra(Intent.EXTRA_PROCESS_TEXT, text))
        finish()
    }

    private fun copyAndClose(text: String) {
        putOnClipboard(text)
        finish()
    }

    private fun putOnClipboard(text: String) {
        getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Hisaab result", text))
    }

    /** Saves the result being shown (for an ambiguous one, the reading chosen) to History. */
    private fun record(state: SelectionUiState) {
        HistoryRecorder.fromSelection(state, System.currentTimeMillis())?.let { (application as HisaabApp).history.add(it) }
    }

    private fun openAppAndClose() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

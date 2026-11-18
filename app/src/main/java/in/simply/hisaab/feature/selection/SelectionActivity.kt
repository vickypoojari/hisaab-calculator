package `in`.simply.hisaab.feature.selection

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
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
import `in`.simply.hisaab.MainActivity
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

        setContent {
            HisaabTheme {
                val base = remember(selectedText, canReplace) { SelectionPresenter.present(selectedText, canReplace) }
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
                            onReplace = ::replaceAndClose,
                            onCopy = ::copyAndClose,
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

    private fun replaceAndClose(text: String) {
        setResult(Activity.RESULT_OK, Intent().putExtra(Intent.EXTRA_PROCESS_TEXT, text))
        finish()
    }

    private fun copyAndClose(text: String) {
        getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Hisaab result", text))
        finish()
    }

    private fun openAppAndClose() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

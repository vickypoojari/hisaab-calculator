package `in`.simply.hisaab.feature.history

import `in`.simply.hisaab.feature.selection.SelectionUiState

/** Decides what is worth saving, and builds the entry. Pure Kotlin. */
object HistoryRecorder {
    fun fromCalculator(expression: String, nowMillis: Long): NewHistoryEntry? = null // stub: tests first

    fun fromSelection(state: SelectionUiState, nowMillis: Long): NewHistoryEntry? = null // stub: tests first
}

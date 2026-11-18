package `in`.simply.hisaab.feature.selection

import `in`.simply.hisaab.parser.ErrorReason

/** Everything the result sheet needs to draw one of the five states on the design board. */
sealed interface SelectionUiState {

    /** A and B: one clear reading. [replaceText] is null when the host field is read-only. */
    data class Success(
        val readAs: String,
        val ignoredWords: List<String>,
        val resultText: String,
        val copyText: String,
        val replaceText: String?,
    ) : SelectionUiState

    /** C: several readings; [selectedIndex] starts at the most likely one. */
    data class Ambiguous(
        val readings: List<ReadingUi>,
        val selectedIndex: Int,
        val ignoredWords: List<String>,
        val canReplace: Boolean,
    ) : SelectionUiState {
        val selected: ReadingUi get() = readings[selectedIndex]
        fun select(index: Int): Ambiguous = copy(selectedIndex = index)
    }

    /** D: no number found. */
    data class NothingToCalculate(val selectedText: String) : SelectionUiState

    /** E: a number was found but the sum cannot be computed. */
    data class Error(val reason: ErrorReason, val selectedText: String) : SelectionUiState
}

data class ReadingUi(
    val readAs: String,
    val resultText: String,
    val copyText: String,
    val replaceText: String?,
)

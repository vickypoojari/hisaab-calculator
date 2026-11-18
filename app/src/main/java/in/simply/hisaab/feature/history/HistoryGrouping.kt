package `in`.simply.hisaab.feature.history

import java.util.TimeZone

sealed interface DayLabel {
    data object Today : DayLabel
    data object Yesterday : DayLabel
    data class Date(val year: Int, val month: Int, val day: Int) : DayLabel
}

data class HistorySection(val day: DayLabel, val entries: List<HistoryEntry>)

object HistoryGrouping {
    fun group(entries: List<HistoryEntry>, nowMillis: Long, timeZone: TimeZone): List<HistorySection> {
        return emptyList() // stub: tests first
    }
}

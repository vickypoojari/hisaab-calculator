package `in`.simply.hisaab.feature.history

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

sealed interface DayLabel {
    data object Today : DayLabel
    data object Yesterday : DayLabel
    data class Date(val year: Int, val month: Int, val day: Int) : DayLabel
}

data class HistorySection(val day: DayLabel, val entries: List<HistoryEntry>)

/** Splits a newest-first list into the day groups the History screen shows. Calendar, not java.time: minSdk 24. */
object HistoryGrouping {
    fun group(entries: List<HistoryEntry>, nowMillis: Long, timeZone: TimeZone): List<HistorySection> {
        val today = dayOf(nowMillis, timeZone)
        val yesterday = dayOf(nowMillis, timeZone, daysBack = 1)
        val sections = LinkedHashMap<DayLabel, MutableList<HistoryEntry>>()
        for (entry in entries) {
            val day = dayOf(entry.createdAt, timeZone)
            val label = when (day) {
                today -> DayLabel.Today
                yesterday -> DayLabel.Yesterday
                else -> day
            }
            sections.getOrPut(label) { mutableListOf() }.add(entry)
        }
        return sections.map { (label, rows) -> HistorySection(label, rows) }
    }

    private fun dayOf(millis: Long, timeZone: TimeZone, daysBack: Int = 0): DayLabel.Date {
        val calendar = GregorianCalendar(timeZone).apply {
            timeInMillis = millis
            add(Calendar.DAY_OF_MONTH, -daysBack)
        }
        return DayLabel.Date(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
    }
}

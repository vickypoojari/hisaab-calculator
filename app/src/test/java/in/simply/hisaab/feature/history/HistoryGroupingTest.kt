package `in`.simply.hisaab.feature.history

import java.util.GregorianCalendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryGroupingTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private val india = TimeZone.getTimeZone("Asia/Kolkata")

    private fun at(year: Int, month: Int, day: Int, hour: Int, zone: TimeZone = utc): Long =
        GregorianCalendar(zone).apply { clear(); set(year, month - 1, day, hour, 0, 0) }.timeInMillis

    private var nextId = 1L
    private fun entry(label: String, millis: Long) =
        HistoryEntry(nextId++, label, "0", "0", HistorySource.CALCULATOR, millis)

    @Test
    fun `nothing in, nothing out`() {
        assertTrue(HistoryGrouping.group(emptyList(), at(2026, 11, 19, 10), utc).isEmpty())
    }

    @Test
    fun `groups newest-first entries into today, yesterday and older days`() {
        val now = at(2026, 11, 19, 10)
        val entries = listOf(
            entry("a", at(2026, 11, 19, 9)),
            entry("b", at(2026, 11, 19, 1)),
            entry("c", at(2026, 11, 18, 23)),
            entry("d", at(2026, 11, 10, 12)),
            entry("e", at(2026, 11, 10, 8)),
        )

        val sections = HistoryGrouping.group(entries, now, utc)

        assertEquals(
            listOf(DayLabel.Today, DayLabel.Yesterday, DayLabel.Date(2026, 11, 10)),
            sections.map { it.day },
        )
        assertEquals(listOf(listOf("a", "b"), listOf("c"), listOf("d", "e")), sections.map { s -> s.entries.map { it.expression } })
    }

    @Test
    fun `days are the viewer's days, not UTC days`() {
        // 2026-11-18 19:00 UTC is 00:30 on the 19th in India.
        val now = at(2026, 11, 19, 10, india)
        val justAfterMidnight = entry("late", at(2026, 11, 18, 19))

        assertEquals(DayLabel.Today, HistoryGrouping.group(listOf(justAfterMidnight), now, india).single().day)
        assertEquals(DayLabel.Yesterday, HistoryGrouping.group(listOf(justAfterMidnight), now + 0, utc).single().day)
    }

    @Test
    fun `a new year and a new month roll over correctly`() {
        val now = at(2027, 1, 1, 10)

        val sections = HistoryGrouping.group(listOf(entry("ny", at(2026, 12, 31, 20)), entry("old", at(2026, 12, 30, 20))), now, utc)

        assertEquals(listOf(DayLabel.Yesterday, DayLabel.Date(2026, 12, 30)), sections.map { it.day })
    }
}

package `in`.simply.hisaab.feature.history

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SqliteHistoryStoreTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val dbName = "history-test.db"

    @Before fun fresh() { context.deleteDatabase(dbName) }
    @After fun cleanUp() { context.deleteDatabase(dbName) }

    private fun store() = SqliteHistoryStore(context, dbName)

    private fun sum(expression: String, at: Long, source: HistorySource = HistorySource.CALCULATOR) =
        NewHistoryEntry(expression, "result of $expression", "42", source, at)

    @Test
    fun an_entry_survives_closing_and_reopening_the_store() {
        store().insert(sum("1 + 1", at = 5))

        val loaded = store().load().single()

        assertEquals("1 + 1", loaded.expression)
        assertEquals("result of 1 + 1", loaded.result)
        assertEquals("42", loaded.value)
        assertEquals(5L, loaded.createdAt)
        assertEquals(HistorySource.CALCULATOR, loaded.source)
    }

    @Test
    fun entries_come_back_newest_first_and_ids_are_unique() {
        val s = store()
        val first = s.insert(sum("1 + 1", at = 1))
        val second = s.insert(sum("2 + 2", at = 2))

        assertEquals(listOf("2 + 2", "1 + 1"), s.load().map { it.expression })
        assertTrue(first.id != second.id)
    }

    @Test
    fun rupee_and_minus_signs_and_the_source_round_trip() {
        val s = store()
        s.insert(NewHistoryEntry("−₹1,200 + ₹450", "−₹750", "-750", HistorySource.SELECTED_TEXT, 9))

        val loaded = store().load().single()

        assertEquals("−₹1,200 + ₹450", loaded.expression)
        assertEquals("−₹750", loaded.result)
        assertEquals(HistorySource.SELECTED_TEXT, loaded.source)
    }

    @Test
    fun delete_removes_only_that_entry() {
        val s = store()
        val doomed = s.insert(sum("1 + 1", at = 1))
        s.insert(sum("2 + 2", at = 2))

        s.delete(doomed.id)

        assertEquals(listOf("2 + 2"), s.load().map { it.expression })
    }

    @Test
    fun clear_removes_everything() {
        val s = store()
        s.insert(sum("1 + 1", at = 1))

        s.clear()

        assertTrue(s.load().isEmpty())
    }

    @Test
    fun trim_keeps_only_the_newest_entries() {
        val s = store()
        (1..5).forEach { s.insert(sum("$it + $it", at = it.toLong())) }

        s.trimTo(2)

        assertEquals(listOf("5 + 5", "4 + 4"), s.load().map { it.expression })
    }
}

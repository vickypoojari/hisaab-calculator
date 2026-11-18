package `in`.simply.hisaab.feature.history

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryRepositoryTest {

    private fun sum(expression: String, result: String, at: Long = 0L, source: HistorySource = HistorySource.CALCULATOR) =
        NewHistoryEntry(expression, result, result.replace(",", ""), source, at)

    @Test
    fun `starts with what the store already holds, newest first`() {
        val store = FakeHistoryStore(listOf(sum("1 + 1", "2", 1), sum("2 + 2", "4", 2)))

        val repo = HistoryRepository(store)

        assertEquals(listOf("2 + 2", "1 + 1"), repo.entries.value.map { it.expression })
    }

    @Test
    fun `a new sum goes on top and is saved to the store`() {
        val store = FakeHistoryStore()
        val repo = HistoryRepository(store)

        repo.add(sum("1 + 1", "2"))
        repo.add(sum("2 + 2", "4"))

        assertEquals(listOf("2 + 2", "1 + 1"), repo.entries.value.map { it.expression })
        assertEquals(2, store.load().size)
    }

    @Test
    fun `the same sum twice in a row is kept once`() {
        val repo = HistoryRepository(FakeHistoryStore())

        repo.add(sum("1 + 1", "2"))
        repo.add(sum("1 + 1", "2"))

        assertEquals(1, repo.entries.value.size)
    }

    @Test
    fun `the same sum from a different source, or after another sum, is kept`() {
        val repo = HistoryRepository(FakeHistoryStore())

        repo.add(sum("1 + 1", "2"))
        repo.add(sum("1 + 1", "2", source = HistorySource.SELECTED_TEXT))
        repo.add(sum("3 + 3", "6"))
        repo.add(sum("1 + 1", "2"))

        assertEquals(4, repo.entries.value.size)
    }

    @Test
    fun `history is capped and the oldest entries go first`() {
        val store = FakeHistoryStore()
        val repo = HistoryRepository(store, maxEntries = 3)

        (1..5).forEach { repo.add(sum("$it + $it", "${it * 2}", at = it.toLong())) }

        assertEquals(listOf("5 + 5", "4 + 4", "3 + 3"), repo.entries.value.map { it.expression })
        assertEquals(3, store.load().size)
    }

    @Test
    fun `deleting removes one entry everywhere`() {
        val store = FakeHistoryStore()
        val repo = HistoryRepository(store)
        repo.add(sum("1 + 1", "2"))
        repo.add(sum("2 + 2", "4"))
        val doomed = repo.entries.value.first { it.expression == "1 + 1" }

        repo.delete(doomed.id)

        assertEquals(listOf("2 + 2"), repo.entries.value.map { it.expression })
        assertEquals(listOf("2 + 2"), store.load().map { it.expression })
    }

    @Test
    fun `clear all empties the list and the store`() {
        val store = FakeHistoryStore()
        val repo = HistoryRepository(store)
        repo.add(sum("1 + 1", "2"))

        repo.clear()

        assertTrue(repo.entries.value.isEmpty())
        assertTrue(store.load().isEmpty())
    }
}

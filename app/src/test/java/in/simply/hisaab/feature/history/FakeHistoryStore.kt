package `in`.simply.hisaab.feature.history

/** In-memory store for JVM tests. Newest first, like the real one. */
class FakeHistoryStore(initial: List<NewHistoryEntry> = emptyList()) : HistoryStore {
    private var nextId = 1L
    private val rows = mutableListOf<HistoryEntry>()

    init {
        initial.forEach { insert(it) }
    }

    override fun load(): List<HistoryEntry> = rows.toList()

    override fun insert(entry: NewHistoryEntry): HistoryEntry {
        val saved = HistoryEntry(nextId++, entry.expression, entry.result, entry.value, entry.source, entry.createdAt)
        rows.add(0, saved)
        return saved
    }

    override fun delete(id: Long) {
        rows.removeAll { it.id == id }
    }

    override fun clear() = rows.clear()

    override fun trimTo(max: Int) {
        while (rows.size > max) rows.removeAt(rows.lastIndex)
    }
}

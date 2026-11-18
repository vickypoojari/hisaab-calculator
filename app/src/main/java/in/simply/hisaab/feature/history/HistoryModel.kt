package `in`.simply.hisaab.feature.history

enum class HistorySource { CALCULATOR, SELECTED_TEXT }

/** A sum about to be saved. [value] is the plain number (ASCII minus) used for Copy and reuse. */
data class NewHistoryEntry(
    val expression: String,
    val result: String,
    val value: String,
    val source: HistorySource,
    val createdAt: Long,
)

data class HistoryEntry(
    val id: Long,
    val expression: String,
    val result: String,
    val value: String,
    val source: HistorySource,
    val createdAt: Long,
)

/** Where history lives. Newest first from [load]. */
interface HistoryStore {
    fun load(): List<HistoryEntry>
    fun insert(entry: NewHistoryEntry): HistoryEntry
    fun delete(id: Long)
    fun clear()
    /** Keeps only the newest [max] entries. */
    fun trimTo(max: Int)
}

/** Keeps nothing. For previews, where there is no database. */
object NoHistoryStore : HistoryStore {
    override fun load(): List<HistoryEntry> = emptyList()
    override fun insert(entry: NewHistoryEntry) = HistoryEntry(0, entry.expression, entry.result, entry.value, entry.source, entry.createdAt)
    override fun delete(id: Long) = Unit
    override fun clear() = Unit
    override fun trimTo(max: Int) = Unit
}

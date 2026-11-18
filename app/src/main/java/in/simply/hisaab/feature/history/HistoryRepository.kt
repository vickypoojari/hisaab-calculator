package `in`.simply.hisaab.feature.history

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * The list the History screen shows. The store is the truth; the list is reloaded from it after
 * every change (at most [maxEntries] rows, so this stays cheap).
 */
class HistoryRepository(private val store: HistoryStore, private val maxEntries: Int = 500) {
    private val state = MutableStateFlow(store.load())

    /** Newest first. */
    val entries: StateFlow<List<HistoryEntry>> = state

    fun add(entry: NewHistoryEntry) {
        val newest = state.value.firstOrNull()
        val repeat = newest != null &&
            newest.expression == entry.expression && newest.result == entry.result && newest.source == entry.source
        if (repeat) return
        store.insert(entry)
        store.trimTo(maxEntries)
        state.value = store.load()
    }

    fun delete(id: Long) {
        store.delete(id)
        state.value = store.load()
    }

    fun clear() {
        store.clear()
        state.value = emptyList()
    }
}

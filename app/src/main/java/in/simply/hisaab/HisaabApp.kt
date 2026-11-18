package `in`.simply.hisaab

import android.app.Application
import `in`.simply.hisaab.feature.history.HistoryRepository
import `in`.simply.hisaab.feature.history.SqliteHistoryStore

/** Owns the one history list shared by the calculator and the selection sheet (same process). */
class HisaabApp : Application() {
    val history: HistoryRepository by lazy { HistoryRepository(SqliteHistoryStore(this)) }
}

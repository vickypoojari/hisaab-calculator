package `in`.simply.hisaab.feature.history

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * History on the phone, in the framework's own SQLite: no extra library and no annotation
 * processing. Order is insertion order (id), so a clock change cannot reshuffle the list.
 */
class SqliteHistoryStore(context: Context, name: String = "history.db") :
    SQLiteOpenHelper(context.applicationContext, name, null, VERSION), HistoryStore {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                expression TEXT NOT NULL,
                result TEXT NOT NULL,
                value TEXT NOT NULL,
                source TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    override fun load(): List<HistoryEntry> = readableDatabase.query(
        TABLE, null, null, null, null, null, "id DESC",
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                add(
                    HistoryEntry(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        expression = cursor.getString(cursor.getColumnIndexOrThrow("expression")),
                        result = cursor.getString(cursor.getColumnIndexOrThrow("result")),
                        value = cursor.getString(cursor.getColumnIndexOrThrow("value")),
                        source = HistorySource.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("source"))),
                        createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
                    ),
                )
            }
        }
    }

    override fun insert(entry: NewHistoryEntry): HistoryEntry {
        val values = ContentValues().apply {
            put("expression", entry.expression)
            put("result", entry.result)
            put("value", entry.value)
            put("source", entry.source.name)
            put("created_at", entry.createdAt)
        }
        val id = writableDatabase.insertOrThrow(TABLE, null, values)
        return HistoryEntry(id, entry.expression, entry.result, entry.value, entry.source, entry.createdAt)
    }

    override fun delete(id: Long) {
        writableDatabase.delete(TABLE, "id = ?", arrayOf(id.toString()))
    }

    override fun clear() {
        writableDatabase.delete(TABLE, null, null)
    }

    override fun trimTo(max: Int) {
        writableDatabase.execSQL("DELETE FROM $TABLE WHERE id NOT IN (SELECT id FROM $TABLE ORDER BY id DESC LIMIT $max)")
    }

    private companion object {
        const val VERSION = 1
        const val TABLE = "history"
    }
}

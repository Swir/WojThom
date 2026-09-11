package com.wojthom.app.data

import android.content.Context
import com.wojthom.app.model.HistorySnapshot
import com.wojthom.app.model.TimeEntry
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class AppStorage(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadHeader(defaultValue: String): String = prefs.getString(KEY_HEADER, defaultValue) ?: defaultValue
    fun saveHeader(value: String): Boolean = prefs.edit().putString(KEY_HEADER, value).commit()

    fun loadLogs(): String = prefs.getString(KEY_LOGS, "") ?: ""
    fun saveLogs(value: String): Boolean = prefs.edit().putString(KEY_LOGS, value).commit()

    fun loadEntries(): List<TimeEntry> = decodeEntries(prefs.getString(KEY_ENTRIES, null))
    fun saveEntries(entries: List<TimeEntry>): Boolean =
        prefs.edit().putString(KEY_ENTRIES, encodeEntries(entries).toString()).commit()

    /**
     * History is the single source of truth. There is deliberately no mutable statistics
     * cache here: every call returns a fresh immutable List so Compose always receives a
     * new instance and can recompose the statistics screen reliably.
     */
    fun loadStatsEntries(): List<TimeEntry> = projectStats(loadHistory()).toList()

    /**
     * Kept for compatibility with the existing UI call site. The old implementation
     * de-duplicated/merged rows in a second database and could drift away from History.
     */
    fun mergeStats(existing: List<TimeEntry>, incoming: List<TimeEntry>): List<TimeEntry> {
        @Suppress("UNUSED_VARIABLE")
        val ignored = existing.size + incoming.size
        return loadStatsEntries().toList()
    }

    /**
     * Empty means "start statistics from zero from now". Non-empty statistics are always
     * derived from History, so we only persist a diagnostic snapshot for compatibility.
     */
    fun saveStatsEntries(entries: List<TimeEntry>): Boolean {
        return if (entries.isEmpty()) {
            prefs.edit()
                .putLong(KEY_STATS_RESET_AT, System.currentTimeMillis())
                .putString(KEY_STATS, "[]")
                .commit()
        } else {
            prefs.edit().putString(KEY_STATS, encodeEntries(entries).toString()).commit()
        }
    }

    fun loadHistory(): List<HistorySnapshot> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = runCatching { array.getJSONObject(index) }.getOrNull() ?: continue
                    val decodedEntries = runCatching {
                        decodeEntries(item.optJSONArray("entries") ?: JSONArray())
                    }.getOrDefault(emptyList())

                    val id = item.optString("id").ifBlank { java.util.UUID.randomUUID().toString() }
                    val savedAt = item.optLong("savedAt", System.currentTimeMillis())
                    val header = item.optString("header")
                    val total = item.optInt("totalMinutes", decodedEntries.sumOf { it.minutes })

                    add(
                        HistorySnapshot(
                            id = id,
                            savedAt = savedAt,
                            header = header,
                            totalMinutes = total,
                            entries = decodedEntries
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    /**
     * Synchronous and verified history write. We use commit() intentionally here because
     * saving a work report is a user action where correctness matters more than a tiny
     * asynchronous write gain. Statistics are written in the same transaction.
     */
    fun saveHistory(history: List<HistorySnapshot>): Boolean {
        val historyJson = encodeHistory(history).toString()
        val projectedStats = projectStats(history)
        val ok = prefs.edit()
            .putString(KEY_HISTORY, historyJson)
            .putString(KEY_STATS, encodeEntries(projectedStats).toString())
            .commit()
        if (!ok) return false

        // Read-back verification protects us from reporting success when persistence failed.
        val reloaded = loadHistory()
        return reloaded.size == history.size &&
            reloaded.map { it.id } == history.map { it.id }
    }

    fun appendHistory(snapshot: HistorySnapshot): List<HistorySnapshot>? {
        val updated = loadHistory() + snapshot
        return if (saveHistory(updated)) loadHistory() else null
    }

    fun resetStatistics(): Boolean = prefs.edit()
        .putLong(KEY_STATS_RESET_AT, System.currentTimeMillis())
        .putString(KEY_STATS, "[]")
        .commit()

    fun clearWorkData(): Boolean = prefs.edit()
        .remove(KEY_HEADER)
        .remove(KEY_LOGS)
        .remove(KEY_ENTRIES)
        .remove(KEY_HISTORY)
        .remove(KEY_STATS)
        .remove(KEY_STATS_RESET_AT)
        .commit()

    private fun projectStats(history: List<HistorySnapshot>): List<TimeEntry> {
        val resetAt = prefs.getLong(KEY_STATS_RESET_AT, Long.MIN_VALUE)
        val activeReports = history.filter { it.savedAt > resetAt }

        // History can contain revisions of the same report. A plain distinct() would drop
        // legitimate identical shifts, while summing every snapshot would double count
        // revisions. We keep the maximum occurrence count of each real shift signature.
        val bestOccurrences = linkedMapOf<String, List<TimeEntry>>()
        activeReports.forEach { snapshot ->
            snapshot.entries
                .filter { it.isValid }
                .groupBy(::statsSignature)
                .forEach { (signature, occurrences) ->
                    val current = bestOccurrences[signature]
                    if (current == null || occurrences.size > current.size) {
                        bestOccurrences[signature] = occurrences.map { it.copy() }
                    }
                }
        }

        return bestOccurrences.values
            .flatten()
            .sortedWith(compareBy<TimeEntry> { it.date }.thenBy { it.client.lowercase() })
            .toList()
    }

    private fun statsSignature(entry: TimeEntry): String = listOf(
        entry.date?.toString().orEmpty(),
        entry.client.trim().lowercase(),
        entry.start.trim(),
        entry.end.trim(),
        entry.minutes.toString()
    ).joinToString("|")

    private fun encodeHistory(history: List<HistorySnapshot>): JSONArray = JSONArray().apply {
        history.forEach { snapshot ->
            put(
                JSONObject().apply {
                    put("id", snapshot.id)
                    put("savedAt", snapshot.savedAt)
                    put("header", snapshot.header)
                    put("totalMinutes", snapshot.totalMinutes)
                    put("entries", encodeEntries(snapshot.entries))
                }
            )
        }
    }

    private fun encodeEntries(entries: List<TimeEntry>): JSONArray = JSONArray().apply {
        entries.forEach { entry ->
            put(
                JSONObject().apply {
                    put("id", entry.id)
                    put("date", entry.date?.toString() ?: JSONObject.NULL)
                    put("client", entry.client)
                    put("start", entry.start)
                    put("end", entry.end)
                    put("minutes", entry.minutes)
                    put("source", entry.source)
                }
            )
        }
    }

    private fun decodeEntries(raw: String?): List<TimeEntry> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { decodeEntries(JSONArray(raw)) }.getOrDefault(emptyList())
    }

    private fun decodeEntries(array: JSONArray): List<TimeEntry> = buildList {
        for (index in 0 until array.length()) {
            val item = runCatching { array.getJSONObject(index) }.getOrNull() ?: continue
            val date = item.optString("date").takeIf { it.isNotBlank() && it != "null" }?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            }
            add(
                TimeEntry(
                    id = item.optString("id").ifBlank { java.util.UUID.randomUUID().toString() },
                    date = date,
                    client = item.optString("client"),
                    start = item.optString("start", "-"),
                    end = item.optString("end", "-"),
                    minutes = item.optInt("minutes"),
                    source = item.optString("source")
                )
            )
        }
    }

    companion object {
        private const val PREFS_NAME = "WojThom_6_DB"
        private const val KEY_HEADER = "header"
        private const val KEY_LOGS = "logs"
        private const val KEY_ENTRIES = "entries"
        private const val KEY_HISTORY = "history"
        private const val KEY_STATS = "stats"
        private const val KEY_STATS_RESET_AT = "stats_reset_at"
    }
}

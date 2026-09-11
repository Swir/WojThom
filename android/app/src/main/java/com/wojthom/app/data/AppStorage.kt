package com.wojthom.app.data

import android.content.Context
import com.wojthom.app.model.HistorySnapshot
import com.wojthom.app.model.TimeEntry
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class AppStorage(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Statistics are a projection of History rather than a second independent database.
     * The same mutable cache instance is rebuilt after every history mutation so the
     * current Compose state never drifts away from what is actually archived.
     */
    private var statsCache: MutableList<TimeEntry>? = null

    fun loadHeader(defaultValue: String): String = prefs.getString(KEY_HEADER, defaultValue) ?: defaultValue
    fun saveHeader(value: String) = prefs.edit().putString(KEY_HEADER, value).apply()

    fun loadLogs(): String = prefs.getString(KEY_LOGS, "") ?: ""
    fun saveLogs(value: String) = prefs.edit().putString(KEY_LOGS, value).apply()

    fun loadEntries(): List<TimeEntry> = decodeEntries(prefs.getString(KEY_ENTRIES, null))
    fun saveEntries(entries: List<TimeEntry>) = prefs.edit().putString(KEY_ENTRIES, encodeEntries(entries).toString()).apply()

    fun loadStatsEntries(): List<TimeEntry> {
        statsCache?.let { return it }
        return rebuildStatsFromHistory(loadHistory())
    }

    /**
     * Clearing statistics starts a new statistics period without deleting History.
     * New reports saved after the reset are counted normally.
     */
    fun saveStatsEntries(entries: List<TimeEntry>) {
        if (entries.isEmpty()) {
            val cache = statsCache ?: mutableListOf<TimeEntry>().also { statsCache = it }
            cache.clear()
            prefs.edit()
                .putLong(KEY_STATS_RESET_AT, System.currentTimeMillis())
                .putString(KEY_STATS, JSONArray().toString())
                .apply()
            return
        }

        val cache = statsCache ?: mutableListOf<TimeEntry>().also { statsCache = it }
        if (cache !== entries) {
            cache.clear()
            cache.addAll(entries.filter { it.isValid })
        }
        persistStatsCache(cache)
    }

    fun loadHistory(): List<HistorySnapshot> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val entries = decodeEntries(item.optJSONArray("entries") ?: JSONArray())
                    add(
                        HistorySnapshot(
                            id = item.optString("id"),
                            savedAt = item.optLong("savedAt"),
                            header = item.optString("header"),
                            totalMinutes = item.optInt("totalMinutes", entries.sumOf { it.minutes }),
                            entries = entries
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveHistory(history: List<HistorySnapshot>) {
        val array = JSONArray()
        history.forEach { snapshot ->
            array.put(
                JSONObject().apply {
                    put("id", snapshot.id)
                    put("savedAt", snapshot.savedAt)
                    put("header", snapshot.header)
                    put("totalMinutes", snapshot.totalMinutes)
                    put("entries", encodeEntries(snapshot.entries))
                }
            )
        }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()

        // Deleting/clearing history now immediately affects statistics as well.
        rebuildStatsFromHistory(history)
    }

    /**
     * Compatibility hook for the current UI. Older builds merged rows into a separate
     * statistics list. We now return the history-derived projection instead.
     */
    fun mergeStats(existing: List<TimeEntry>, incoming: List<TimeEntry>): List<TimeEntry> {
        @Suppress("UNUSED_VARIABLE")
        val ignored = existing.size + incoming.size
        return loadStatsEntries()
    }

    fun clearWorkData() {
        prefs.edit()
            .remove(KEY_HEADER)
            .remove(KEY_LOGS)
            .remove(KEY_ENTRIES)
            .remove(KEY_HISTORY)
            .remove(KEY_STATS)
            .remove(KEY_STATS_RESET_AT)
            .apply()
        statsCache = null
    }

    private fun rebuildStatsFromHistory(history: List<HistorySnapshot>): MutableList<TimeEntry> {
        val resetAt = prefs.getLong(KEY_STATS_RESET_AT, Long.MIN_VALUE)
        val activeReports = history.filter { it.savedAt > resetAt }

        /*
         * History contains snapshots/revisions. Summing every snapshot would count the
         * same real shift several times when a report is saved, edited and saved again.
         * A plain distinct() is also wrong because two genuinely identical rows can be
         * present in one report. We therefore calculate a multiset union: for every
         * shift signature we keep the largest occurrence count found in any snapshot.
         */
        val bestOccurrences = linkedMapOf<String, List<TimeEntry>>()
        activeReports.forEach { snapshot ->
            snapshot.entries
                .filter { it.isValid }
                .groupBy(::statsSignature)
                .forEach { (signature, occurrences) ->
                    val current = bestOccurrences[signature]
                    if (current == null || occurrences.size > current.size) {
                        bestOccurrences[signature] = occurrences
                    }
                }
        }

        val rebuilt = bestOccurrences.values
            .flatten()
            .sortedWith(compareBy<TimeEntry> { it.date }.thenBy { it.client.lowercase() })

        val cache = statsCache ?: mutableListOf<TimeEntry>().also { statsCache = it }
        cache.clear()
        cache.addAll(rebuilt)
        persistStatsCache(cache)
        return cache
    }

    private fun statsSignature(entry: TimeEntry): String = listOf(
        entry.date?.toString().orEmpty(),
        entry.client.trim().lowercase(),
        entry.start.trim(),
        entry.end.trim(),
        entry.minutes.toString()
    ).joinToString("|")

    private fun persistStatsCache(entries: List<TimeEntry>) {
        prefs.edit().putString(KEY_STATS, encodeEntries(entries).toString()).apply()
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
            val item = array.getJSONObject(index)
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

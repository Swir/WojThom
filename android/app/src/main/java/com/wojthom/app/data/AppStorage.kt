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
     * Statistics are now a projection of History rather than an independent database.
     * Keeping one mutable cache is intentional: the Compose screen receives this list
     * once, while save/delete operations rebuild the same list instance underneath it.
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
     * An empty list means the user deliberately reset statistics. We keep History,
     * but remember the reset timestamp so only reports saved afterwards count again.
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

        // One source of truth: every history mutation immediately rebuilds stats.
        rebuildStatsFromHistory(history)
    }

    /**
     * Kept for compatibility with the current UI call-site. Previous versions merged
     * and de-duplicated individual rows here, which could silently lose legitimate
     * identical shifts. Statistics now come from saved reports exactly once per row.
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
        val rebuilt = history
            .asSequence()
            .filter { it.savedAt > resetAt }
            .flatMap { it.entries.asSequence() }
            .filter { it.isValid }
            .sortedWith(compareBy<TimeEntry> { it.date }.thenBy { it.client.lowercase() })
            .toList()

        val cache = statsCache ?: mutableListOf<TimeEntry>().also { statsCache = it }
        cache.clear()
        cache.addAll(rebuilt)
        persistStatsCache(cache)
        return cache
    }

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

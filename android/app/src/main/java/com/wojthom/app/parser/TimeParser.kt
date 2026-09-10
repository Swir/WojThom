package com.wojthom.app.parser

import com.wojthom.app.model.TimeEntry
import java.time.DateTimeException
import java.time.LocalDate
import java.time.Year
import kotlin.math.roundToInt

object TimeParser {
    private val dateRegex = Regex("""^(\d{1,2})[.\-/\s]+(\d{1,2})(?:[.\-/\s]+(\d{2,4}))?\b""")
    private val rangeRegex = Regex("""\b(\d{1,2})[:.](\d{2})\s*-\s*(\d{1,2})[:.](\d{2})\b""")
    private val singleClockRegex = Regex("""\b(\d{1,2})[:.]([0-5]\d)\b""")
    private val hoursRegex = Regex("""\b(\d+(?:[.,]\d+)?)\s*[hHtT]\b""")
    private val trailingDecimalRegex = Regex("""(?:^|\s)(\d+(?:[.,]\d+)?)\s*$""")

    fun parse(raw: String, defaultYear: Int = Year.now().value): List<TimeEntry> =
        raw.lineSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .map { parseLine(it, defaultYear) }
            .toList()

    fun parseLine(source: String, defaultYear: Int = Year.now().value): TimeEntry {
        var rest = source.trim()
        var date: LocalDate? = null
        var start = "-"
        var end = "-"
        var minutes = 0

        dateRegex.find(rest)?.let { match ->
            val day = match.groupValues[1].toIntOrNull()
            val month = match.groupValues[2].toIntOrNull()
            val rawYear = match.groupValues[3]
            val year = when {
                rawYear.isBlank() -> defaultYear
                rawYear.length == 2 -> 2000 + (rawYear.toIntOrNull() ?: 0)
                else -> rawYear.toIntOrNull() ?: defaultYear
            }

            if (day != null && month != null) {
                date = try {
                    LocalDate.of(year, month, day)
                } catch (_: DateTimeException) {
                    null
                }
            }
            rest = rest.removeRange(match.range).trim()
        }

        val range = rangeRegex.find(rest)
        if (range != null) {
            val sh = range.groupValues[1].toIntOrNull() ?: -1
            val sm = range.groupValues[2].toIntOrNull() ?: -1
            val eh = range.groupValues[3].toIntOrNull() ?: -1
            val em = range.groupValues[4].toIntOrNull() ?: -1

            if (sh in 0..23 && eh in 0..23 && sm in 0..59 && em in 0..59) {
                start = "%02d:%02d".format(sh, sm)
                end = "%02d:%02d".format(eh, em)
                val startMinutes = sh * 60 + sm
                var endMinutes = eh * 60 + em
                if (endMinutes < startMinutes) endMinutes += 24 * 60
                minutes = endMinutes - startMinutes
            }
            rest = rest.removeRange(range.range).trim()
        } else {
            val hours = hoursRegex.find(rest)
            val singleClock = singleClockRegex.find(rest)
            val trailingDecimal = trailingDecimalRegex.find(rest)

            when {
                hours != null -> {
                    minutes = decimalHoursToMinutes(hours.groupValues[1])
                    rest = rest.removeRange(hours.range).trim()
                }
                singleClock != null -> {
                    val h = singleClock.groupValues[1].toIntOrNull() ?: -1
                    val m = singleClock.groupValues[2].toIntOrNull() ?: -1
                    if (h >= 0 && m in 0..59) minutes = h * 60 + m
                    rest = rest.removeRange(singleClock.range).trim()
                }
                trailingDecimal != null -> {
                    minutes = decimalHoursToMinutes(trailingDecimal.groupValues[1])
                    rest = rest.removeRange(trailingDecimal.range).trim()
                }
            }
        }

        return TimeEntry(
            date = date,
            client = rest.trim(' ', '-', '|'),
            start = start,
            end = end,
            minutes = minutes,
            source = source
        )
    }

    private fun decimalHoursToMinutes(value: String): Int {
        val decimal = value.replace(',', '.').toDoubleOrNull() ?: return 0
        if (decimal < 0 || decimal > 24) return 0
        return (decimal * 60.0).roundToInt()
    }
}

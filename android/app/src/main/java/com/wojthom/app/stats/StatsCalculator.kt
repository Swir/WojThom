package com.wojthom.app.stats

import com.wojthom.app.model.TimeEntry
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields

private const val WEEKLY_TARGET_MINUTES = 37 * 60 + 30

data class MonthStats(
    val month: YearMonth,
    val minutes: Int,
    val entries: Int
)

data class DashboardStats(
    val totalMinutes: Int,
    val thisWeekMinutes: Int,
    val thisMonthMinutes: Int,
    val entryCount: Int,
    val clientCount: Int,
    val weeklyTargetMinutes: Int = WEEKLY_TARGET_MINUTES,
    val months: List<MonthStats> = emptyList()
) {
    val weeklyProgress: Float
        get() = if (weeklyTargetMinutes <= 0) 0f else (thisWeekMinutes.toFloat() / weeklyTargetMinutes).coerceIn(0f, 1f)

    val weeklyDifferenceMinutes: Int
        get() = thisWeekMinutes - weeklyTargetMinutes
}

object StatsCalculator {
    fun calculate(entries: List<TimeEntry>, today: LocalDate = LocalDate.now()): DashboardStats {
        val valid = entries.filter { it.isValid && it.date != null }
        val weekFields = WeekFields.ISO
        val currentWeek = today.get(weekFields.weekOfWeekBasedYear())
        val currentWeekYear = today.get(weekFields.weekBasedYear())
        val currentMonth = YearMonth.from(today)

        val thisWeek = valid.filter { entry ->
            val date = entry.date ?: return@filter false
            date.get(weekFields.weekOfWeekBasedYear()) == currentWeek &&
                date.get(weekFields.weekBasedYear()) == currentWeekYear
        }.sumOf { it.minutes }

        val thisMonth = valid.filter { entry ->
            entry.date?.let { YearMonth.from(it) == currentMonth } == true
        }.sumOf { it.minutes }

        val months = valid
            .groupBy { YearMonth.from(requireNotNull(it.date)) }
            .map { (month, monthEntries) ->
                MonthStats(
                    month = month,
                    minutes = monthEntries.sumOf { it.minutes },
                    entries = monthEntries.size
                )
            }
            .sortedByDescending { it.month }

        return DashboardStats(
            totalMinutes = valid.sumOf { it.minutes },
            thisWeekMinutes = thisWeek,
            thisMonthMinutes = thisMonth,
            entryCount = valid.size,
            clientCount = valid.map { it.client.trim().lowercase() }.filter { it.isNotBlank() }.distinct().size,
            months = months
        )
    }
}

package com.wojthom.app.model

import java.time.LocalDate
import java.util.UUID

data class TimeEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate?,
    val client: String,
    val start: String = "-",
    val end: String = "-",
    val minutes: Int = 0,
    val source: String = ""
) {
    val isValid: Boolean
        get() = date != null && client.isNotBlank() && minutes > 0

    val durationText: String
        get() = "%d:%02d".format(minutes / 60, minutes % 60)
}

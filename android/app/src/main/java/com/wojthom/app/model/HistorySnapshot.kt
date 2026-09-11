package com.wojthom.app.model

import java.util.UUID

data class HistorySnapshot(
    val id: String = UUID.randomUUID().toString(),
    val savedAt: Long = System.currentTimeMillis(),
    val header: String,
    val totalMinutes: Int,
    val entries: List<TimeEntry>
)

package com.iptvapp.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EpgProgram(
    val channelId: String,
    val title: String,
    val description: String = "",
    val startTime: Long,
    val endTime: Long
) {
    val isLive: Boolean get() {
        val now = System.currentTimeMillis()
        return now in startTime..endTime
    }

    val progressFraction: Float get() {
        if (!isLive) return 0f
        val now = System.currentTimeMillis()
        return ((now - startTime).toFloat() / (endTime - startTime).toFloat()).coerceIn(0f, 1f)
    }

    val timeLabel: String get() {
        val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        return "${fmt.format(Date(startTime))} - ${fmt.format(Date(endTime))}"
    }
}

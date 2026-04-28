package org.kth.countryguesser.util

import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date

fun getCurrentDateFromFirebase(): LocalDate {
    return Timestamp.now().toDate().toInstant().atZone(ZoneOffset.UTC).toLocalDate()
}

fun getCurrentTimeFromFirebase(): Date {
    return Timestamp.now().toDate()
}

/**
 * Calculates the time in milliseconds until the next day starts (00:00 UTC).
 */
fun getTimeUntilNextDay(): Long {
    val currentTime = getCurrentTimeFromFirebase().time
    val nextDayStart = getCurrentDateFromFirebase()
        .plusDays(1)
        .atStartOfDay(ZoneId.of("UTC"))
        .toInstant()
        .toEpochMilli()
    return nextDayStart - currentTime
}

/**
 * Formats milliseconds to HH:MM:SS format for countdown display.
 */
fun formatCountdownTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

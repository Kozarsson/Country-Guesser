package org.kth.countryguesser.util

import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

fun getCurrentDateFromFirebase(): LocalDate {
    return Timestamp.now().toDate().toInstant().atZone(ZoneOffset.UTC).toLocalDate()
}
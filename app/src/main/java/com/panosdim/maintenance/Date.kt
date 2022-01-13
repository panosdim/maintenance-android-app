package com.panosdim.maintenance

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

val showDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
val firebaseDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun LocalDate.toEpochMilli(): Long {
    return this.toEpochDay() * (1000 * 60 * 60 * 24)
}

fun fromEpochMilli(date: Long): LocalDate {
    return LocalDate.ofEpochDay(date / (1000 * 60 * 60 * 24))
}

fun LocalDate.toFormattedString(): String {
    return this.format(showDateFormatter)
}

fun String.toLocalDate(): LocalDate {
    return try {
        LocalDate.parse(
            this,
            firebaseDateFormatter
        )
    } catch (ex: DateTimeParseException) {
        LocalDate.now()
    }
}
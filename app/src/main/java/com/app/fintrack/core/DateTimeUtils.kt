package com.app.fintrack.core

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun appTimeZone(): TimeZone = TimeZone.currentSystemDefault()

@OptIn(ExperimentalTime::class)
fun currentLocalDate(timeZone: TimeZone = appTimeZone()): LocalDate =
    Clock.System.now().toLocalDateTime(timeZone).date

@OptIn(ExperimentalTime::class)
fun currentLocalDateTime(timeZone: TimeZone = appTimeZone()): LocalDateTime =
    Clock.System.now().toLocalDateTime(timeZone)

fun LocalDate.minusDaysSafe(days: Int): LocalDate = this + DatePeriod(days = -days)

fun LocalDate.minusMonthsSafe(months: Int): LocalDate = this + DatePeriod(months = -months)

fun LocalDate.plusMonthsSafe(months: Int): LocalDate = this + DatePeriod(months = months)

fun LocalDate.atStartOfMonth(): LocalDate = LocalDate(year, month, 1)

fun LocalDate.atStartOfWeekMonday(): LocalDate {
    val daysSinceMonday = dayOfWeek.daysSinceMonday()
    return minusDaysSafe(daysSinceMonday)
}

private fun DayOfWeek.daysSinceMonday(): Int {
    return when (this) {
        DayOfWeek.MONDAY -> 0
        DayOfWeek.TUESDAY -> 1
        DayOfWeek.WEDNESDAY -> 2
        DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 4
        DayOfWeek.SATURDAY -> 5
        DayOfWeek.SUNDAY -> 6
    }
}

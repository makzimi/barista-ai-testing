package ru.otus.barista.core.testing

import ru.otus.barista.core.common.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

/** A [Clock] that always returns [time] (on [date] in [zoneId]). Mutable so a test can move time forward. */
class FixedClock(
    var time: LocalTime = LocalTime.NOON,
    private val date: LocalDate = LocalDate.of(2026, 10, 11),
    private val zoneId: ZoneId = ZoneOffset.UTC,
) : Clock {
    override fun now(): Instant = date.atTime(time).atZone(zoneId).toInstant()

    override fun zone(): ZoneId = zoneId
}

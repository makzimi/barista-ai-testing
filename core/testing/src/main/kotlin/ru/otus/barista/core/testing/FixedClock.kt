package ru.otus.barista.core.testing

import ru.otus.barista.core.common.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * A [Clock] that always returns [time] (on [date] in [zoneId]). Mutable so a test can move time forward:
 * assign [time] directly or call [advanceBy].
 */
class FixedClock(
    var time: LocalTime = LocalTime.NOON,
    private var date: LocalDate = LocalDate.of(2026, 10, 11),
    private val zoneId: ZoneId = ZoneOffset.UTC,
) : Clock {

    /** `FixedClock(Instant.parse("2026-10-11T14:00:00Z"))` — the moment [instant] seen in [zone]. */
    constructor(instant: Instant, zone: ZoneId = MOSCOW) : this(
        time = instant.atZone(zone).toLocalTime(),
        date = instant.atZone(zone).toLocalDate(),
        zoneId = zone,
    )

    override fun now(): Instant = date.atTime(time).atZone(zoneId).toInstant()

    override fun zone(): ZoneId = zoneId

    /** Moves the clock forward by [duration] (crossing midnight moves the date too). */
    fun advanceBy(duration: Duration) {
        val next = now().plus(duration).atZone(zoneId)
        date = next.toLocalDate()
        time = next.toLocalTime()
    }

    companion object {
        val MOSCOW: ZoneId = ZoneId.of("Europe/Moscow")
    }
}

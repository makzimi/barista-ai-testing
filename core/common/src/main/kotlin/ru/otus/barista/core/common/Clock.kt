package ru.otus.barista.core.common

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * Source of "now". Inject it instead of calling `Instant.now()` / `LocalTime.now()` so that
 * time-dependent rules (happy hour) are deterministic in tests (see `FixedClock` in `:core:testing`).
 */
interface Clock {
    fun now(): Instant

    fun zone(): ZoneId

    fun localTime(): LocalTime = now().atZone(zone()).toLocalTime()
}

class SystemClock(private val zoneId: ZoneId = ZoneId.systemDefault()) : Clock {
    override fun now(): Instant = Instant.now()

    override fun zone(): ZoneId = zoneId
}

package ru.otus.barista.domain.model

import java.time.LocalTime

/**
 * Server-provided discount window of a drink (rule 1): [percent] off while the local time is in `[from, to)`.
 * A window whose [to] is before [from] wraps around midnight (e.g. 22:00–02:00). `from == to` is an empty window.
 */
data class HappyHour(
    val from: LocalTime,
    val to: LocalTime,
    val percent: Int,
) {
    fun isActiveAt(time: LocalTime): Boolean = when {
        from < to -> time >= from && time < to
        from > to -> time >= from || time < to
        else -> false
    }
}

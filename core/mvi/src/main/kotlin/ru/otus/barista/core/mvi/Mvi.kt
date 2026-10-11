package ru.otus.barista.core.mvi

/** Marker for screen state. Implementations: `@Immutable data class *State` in a `presentation` package. */
interface MviState

/** Marker for user intents. Implementations: `sealed interface *Action`. */
interface MviAction

/** Marker for one-shot effects (navigation, snackbar). Implementations: `sealed interface *Event`. */
interface MviEvent

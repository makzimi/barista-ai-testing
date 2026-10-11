package ru.otus.barista.core.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
 * Minimal MVI base: one [state] stream, one stream of one-shot [events], one entry point [onAction].
 *
 * Events are buffered in a [Channel], so an event sent before the UI subscribes is not lost and
 * every event is delivered to exactly one collector.
 */
abstract class MviViewModel<S : MviState, A : MviAction, E : MviEvent>(initialState: S) : ViewModel() {

    private val mutableState = MutableStateFlow(initialState)
    val state: StateFlow<S> = mutableState.asStateFlow()

    private val eventChannel = Channel<E>(Channel.BUFFERED)
    val events: Flow<E> = eventChannel.receiveAsFlow()

    protected val currentState: S
        get() = mutableState.value

    abstract fun onAction(action: A)

    protected fun updateState(reducer: S.() -> S) {
        mutableState.update(reducer)
    }

    protected fun sendEvent(event: E) {
        eventChannel.trySend(event)
    }
}

package ru.otus.barista.app.fakebackend

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Debug switches of the fake backend. Toggled from the overflow menu of the menu screen. */
class FakeBackendConfig {
    private val failNext = MutableStateFlow(false)

    /** When `true`, the next request gets HTTP 500 and the flag resets itself. */
    val failNextRequest: StateFlow<Boolean> = failNext.asStateFlow()

    fun setFailNextRequest(value: Boolean) {
        failNext.value = value
    }

    /** Returns `true` (once) if a failure was requested. */
    fun consumeFailure(): Boolean = failNext.compareAndSet(expect = true, update = false)
}

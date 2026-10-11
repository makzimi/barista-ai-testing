package ru.otus.barista.core.testing

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestDispatcher
import ru.otus.barista.core.common.DispatcherProvider

/** All dispatchers are the same [testDispatcher], so `advanceTimeBy` / `advanceUntilIdle` control everything. */
class TestDispatcherProvider(testDispatcher: TestDispatcher) : DispatcherProvider {
    override val main: CoroutineDispatcher = testDispatcher
    override val io: CoroutineDispatcher = testDispatcher
    override val default: CoroutineDispatcher = testDispatcher
}

package ru.otus.barista.core.common

import kotlin.coroutines.cancellation.CancellationException

/**
 * Like [runCatching], but never swallows [CancellationException]: structured concurrency keeps working
 * when a coroutine that wraps a suspend call in a [Result] is cancelled.
 */
@Suppress("TooGenericExceptionCaught")
inline fun <T> runSuspendCatching(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Throwable) {
        Result.failure(error)
    }

/** Chains a [Result]-returning call, propagating the first failure. */
inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> =
    fold(onSuccess = transform, onFailure = { Result.failure(it) })

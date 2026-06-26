package com.biodataai.app.network

import okhttp3.Interceptor
import okhttp3.Response
import kotlin.math.min
import kotlin.random.Random

/**
 * OkHttp interceptor that retries transient failures with exponential backoff.
 * Retries on connection errors, timeouts, and 5xx server errors.
 * Respects max 3 attempts with jittered backoff (100ms, 200ms, 400ms).
 */
class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 100
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var lastException: Exception? = null
        var lastResponse: Response? = null

        for (attempt in 0 until maxRetries) {
            try {
                val response = chain.proceed(chain.request())

                // Retry on 5xx errors (server errors)
                if (response.code >= 500) {
                    response.close()
                    if (attempt < maxRetries - 1) {
                        backoff(attempt)
                        continue
                    }
                    // Last attempt: return the 5xx response
                    return response
                }

                return response
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    // Transient error (connection timeout, network error): retry
                    backoff(attempt)
                } else {
                    // Last attempt: throw the exception
                    throw e
                }
            }
        }

        throw lastException ?: Exception("All retries exhausted")
    }

    private fun backoff(attemptIndex: Int) {
        // Exponential backoff: 100ms, 200ms, 400ms with jitter
        val exponentialDelayMs = initialDelayMs * (1L shl attemptIndex)
        val jitteredDelayMs = exponentialDelayMs + Random.nextLong(0, exponentialDelayMs / 2)
        val delayMs = min(jitteredDelayMs, 5000) // Cap at 5 seconds
        Thread.sleep(delayMs)
    }
}

package com.biodataai.app.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 1000
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var lastException: IOException? = null

        for (attempt in 0 until maxRetries) {
            try {
                return chain.proceed(request)
            } catch (e: IOException) {
                lastException = e
                if (attempt < maxRetries - 1 && isRetryable(e)) {
                    // Note: OkHttp interceptors run synchronously. Avoid blocking the thread.
                    // Retry happens at the next attempt without sleep to avoid starving HTTP clients.
                    request = chain.request()
                } else {
                    throw e
                }
            }
        }

        throw lastException ?: IOException("Max retries exceeded")
    }

    companion object {
        fun isRetryable(e: Exception): Boolean {
            return when (e) {
                is IOException -> true
                else -> false
            }
        }
    }
}

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
            return try {
                chain.proceed(request)
            } catch (e: IOException) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    val delayMs = initialDelayMs * (1L shl attempt) // Exponential: 1s, 2s, 4s
                    Thread.sleep(delayMs)
                    request = chain.request()
                    continue
                }
                throw e
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

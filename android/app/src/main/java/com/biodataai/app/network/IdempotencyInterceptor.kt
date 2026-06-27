package com.biodataai.app.network

import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID

class IdempotencyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Add Idempotency-Key header to POST and PUT requests to /api/biodatas
        if ((originalRequest.method == "POST" || originalRequest.method == "PUT") &&
            originalRequest.url.encodedPath.contains("/api/biodatas")) {
            val requestWithKey = originalRequest.newBuilder()
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .build()
            return chain.proceed(requestWithKey)
        }

        return chain.proceed(originalRequest)
    }
}

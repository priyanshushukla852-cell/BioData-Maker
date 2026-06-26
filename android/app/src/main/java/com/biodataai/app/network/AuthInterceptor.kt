package com.biodataai.app.network

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val firebaseAuth: FirebaseAuth) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth header for public endpoints (login, OTP verification, etc.)
        if (isPublicEndpoint(originalRequest.url.encodedPath)) {
            return chain.proceed(originalRequest)
        }

        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            return chain.proceed(originalRequest)
        }

        // Get Firebase ID token synchronously (blocking on worker thread)
        return try {
            val idToken = blockingGetIdToken(currentUser)
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $idToken")
                .build()
            chain.proceed(authenticatedRequest)
        } catch (e: Exception) {
            // If token fetch fails, proceed without auth header — backend will reject with 401
            chain.proceed(originalRequest)
        }
    }

    private fun blockingGetIdToken(user: com.google.firebase.auth.FirebaseUser): String {
        val task = user.getIdToken(false)
        while (!task.isComplete) {
            Thread.sleep(10)
        }
        if (task.isSuccessful) {
            return task.result?.token ?: throw Exception("No token in result")
        } else {
            throw task.exception ?: Exception("Token fetch failed")
        }
    }

    private fun isPublicEndpoint(path: String): Boolean {
        return path.contains("/api/auth/") || path.contains("/api/templates")
    }
}

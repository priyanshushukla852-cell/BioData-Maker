package com.biodataai.app.network

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://localhost:8080/" // Development; switch to production URL at build time

    fun getRetrofit(context: Context): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // HEADERS only: never log BODY to avoid exposing PII in logs (CLAUDE.md rule 2)
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(FirebaseAuth.getInstance()))
            .addInterceptor(IdempotencyInterceptor())
            .addInterceptor(RetryInterceptor(maxRetries = 3, initialDelayMs = 100))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

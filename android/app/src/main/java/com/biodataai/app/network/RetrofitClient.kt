package com.biodataai.app.network

import android.content.Context
import com.biodataai.app.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Backend base URL is set per build type via buildConfigField (see app/build.gradle.kts).
    private const val BASE_URL = BuildConfig.API_BASE_URL

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

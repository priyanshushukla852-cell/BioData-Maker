package com.biodataai.app.network.api

import retrofit2.http.Body
import retrofit2.http.POST

data class VerifyOtpRequest(
    val phoneNumber: String,
    val otp: String
)

data class VerifyOtpResponse(
    val firebaseCustomToken: String
)

interface AuthService {
    @POST("/api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): VerifyOtpResponse
}

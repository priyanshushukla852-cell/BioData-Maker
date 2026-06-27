package com.biodataai.app.network.api

import retrofit2.http.Body
import retrofit2.http.POST

data class VerifyTokenRequest(
    val idToken: String
)

data class VerifyTokenResponse(
    val userId: String,
    val isNewUser: Boolean,
    val displayName: String?
)

interface AuthService {
    @POST("/api/auth/verify-token")
    suspend fun verifyToken(@Body request: VerifyTokenRequest): VerifyTokenResponse
}

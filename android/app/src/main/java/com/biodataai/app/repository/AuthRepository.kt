package com.biodataai.app.repository

import android.content.Context
import com.biodataai.app.core.Result
import com.biodataai.app.network.RetrofitClient
import com.biodataai.app.network.api.AuthService
import com.biodataai.app.network.api.VerifyOtpRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth
) {

    private val authService: AuthService by lazy {
        RetrofitClient.getRetrofit(context).create(AuthService::class.java)
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Google sign-in failed: ${e.message}")
        }
    }

    suspend fun verifyPhoneOtp(phoneNumber: String, otp: String): Result<Unit> {
        return try {
            val response = authService.verifyOtp(VerifyOtpRequest(phoneNumber, otp))
            firebaseAuth.signInWithCustomToken(response.firebaseCustomToken).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "OTP verification failed: ${e.message}")
        }
    }

    fun getCurrentUser() = firebaseAuth.currentUser

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}

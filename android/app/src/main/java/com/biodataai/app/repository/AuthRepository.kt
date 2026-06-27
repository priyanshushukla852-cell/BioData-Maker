package com.biodataai.app.repository

import android.content.Context
import com.biodataai.app.core.Result
import com.biodataai.app.network.RetrofitClient
import com.biodataai.app.network.api.AuthService
import com.biodataai.app.network.api.VerifyTokenRequest
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
            val user = firebaseAuth.currentUser
                ?: return Result.Error(Exception("User is null after Google sign-in"), "Sign-in failed")
            val token = user.getIdToken(false).await().token
                ?: return Result.Error(Exception("ID token is null"), "Could not get ID token")
            authService.verifyToken(VerifyTokenRequest(token))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Google sign-in failed: ${e.message}")
        }
    }

    suspend fun verifyPhoneOtp(verificationId: String, otp: String): Result<Unit> {
        return try {
            val credential = com.google.firebase.auth.PhoneAuthProvider.getCredential(verificationId, otp)
            firebaseAuth.signInWithCredential(credential).await()
            val user = firebaseAuth.currentUser
                ?: return Result.Error(Exception("User is null after OTP sign-in"), "Sign-in failed")
            val token = user.getIdToken(false).await().token
                ?: return Result.Error(Exception("ID token is null"), "Could not get ID token")
            authService.verifyToken(VerifyTokenRequest(token))
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

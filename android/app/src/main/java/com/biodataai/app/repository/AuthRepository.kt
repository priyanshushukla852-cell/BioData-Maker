package com.biodataai.app.repository

import android.app.Activity
import android.content.Context
import com.biodataai.app.core.Result
import com.biodataai.app.network.RetrofitClient
import com.biodataai.app.network.api.AuthService
import com.biodataai.app.network.api.VerifyTokenRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

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

    /**
     * Starts Firebase Phone Auth: sends an SMS code to [phoneNumber] (E.164, e.g. +9198...).
     * Results arrive asynchronously on [callbacks] (onCodeSent → verificationId,
     * onVerificationCompleted → auto-retrieval, onVerificationFailed). Requires an Activity for
     * the reCAPTCHA/Play Integrity fallback.
     */
    fun sendOtp(
        activity: Activity,
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun verifyPhoneOtp(verificationId: String, otp: String): Result<Unit> {
        return signInWithPhoneCredential(PhoneAuthProvider.getCredential(verificationId, otp))
    }

    /** Completes phone sign-in from a credential (manual OTP entry or SMS auto-retrieval). */
    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): Result<Unit> {
        return try {
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

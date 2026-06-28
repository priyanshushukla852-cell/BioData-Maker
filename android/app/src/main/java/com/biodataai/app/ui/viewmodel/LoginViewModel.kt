package com.biodataai.app.ui.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.biodataai.app.core.Result
import com.biodataai.app.repository.AuthRepository
import com.biodataai.app.ui.base.BaseViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val phoneOtpFlow: PhoneOtpFlow? = null
)

data class PhoneOtpFlow(
    val step: Int, // 1: enter phone, 2: enter OTP
    val phoneNumber: String = "",
    val verificationId: String? = null
)

class LoginViewModel(
    context: Context,
    firebaseAuth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<LoginUiState>(savedStateHandle) {

    private val authRepository = AuthRepository(context, firebaseAuth)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signInWithGoogle(idToken: String) {
        launchAsync {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Sign-in failed"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun startPhoneOtp() {
        _uiState.value = _uiState.value.copy(
            phoneOtpFlow = PhoneOtpFlow(step = 1)
        )
    }

    fun submitPhoneNumber(activity: Activity, phoneNumber: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null,
            phoneOtpFlow = (_uiState.value.phoneOtpFlow ?: PhoneOtpFlow(step = 1)).copy(phoneNumber = phoneNumber)
        )
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Instant verification / SMS auto-retrieval — sign in without manual OTP entry.
                launchAsync {
                    when (val result = authRepository.signInWithPhoneCredential(credential)) {
                        is Result.Success -> _uiState.value = _uiState.value.copy(isLoading = false, phoneOtpFlow = null)
                        is Result.Error -> _uiState.value =
                            _uiState.value.copy(isLoading = false, error = result.message ?: "Verification failed")
                        is Result.Loading -> {}
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Verification failed: ${e.message}")
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    phoneOtpFlow = PhoneOtpFlow(step = 2, phoneNumber = phoneNumber, verificationId = verificationId)
                )
            }
        }
        authRepository.sendOtp(activity, phoneNumber, callbacks)
    }

    fun submitOtp(otp: String) {
        launchAsync {
            val verificationId = _uiState.value.phoneOtpFlow?.verificationId
            if (verificationId == null) {
                _uiState.value = _uiState.value.copy(error = "Missing verification id. Request a new code.")
                return@launchAsync
            }
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = authRepository.verifyPhoneOtp(verificationId, otp)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        phoneOtpFlow = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "OTP verification failed"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun cancelPhoneOtp() {
        _uiState.value = _uiState.value.copy(phoneOtpFlow = null)
    }
}

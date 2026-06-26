package com.biodataai.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.biodataai.app.core.Result
import com.biodataai.app.repository.AuthRepository
import com.biodataai.app.ui.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val googleSignInInitiated: Boolean = false,
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

    fun startGoogleSignIn() {
        _uiState.value = _uiState.value.copy(googleSignInInitiated = true)
    }

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

    fun submitPhoneNumber(phoneNumber: String) {
        // In a real app, trigger Firebase Phone Auth here to get verificationId
        // For now, simulate requesting OTP from backend
        _uiState.value = _uiState.value.copy(
            phoneOtpFlow = _uiState.value.phoneOtpFlow?.copy(
                step = 2,
                phoneNumber = phoneNumber,
                verificationId = "mock_verification_id"
            )
        )
    }

    fun submitOtp(otp: String) {
        launchAsync {
            val phoneNumber = _uiState.value.phoneOtpFlow?.phoneNumber ?: return@launchAsync
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = authRepository.verifyPhoneOtp(phoneNumber, otp)) {
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

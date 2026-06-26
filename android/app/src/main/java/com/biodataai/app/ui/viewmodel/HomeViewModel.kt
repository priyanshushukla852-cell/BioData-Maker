package com.biodataai.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.db.entity.BiodataEntity
import com.biodataai.app.repository.AuthRepository
import com.biodataai.app.repository.BiodataRepository
import com.biodataai.app.ui.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userName: String? = null,
    val biodatas: List<BiodataEntity> = emptyList()
)

class HomeViewModel(
    context: Context,
    firebaseAuth: FirebaseAuth,
    database: BioDataDatabase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<HomeUiState>(savedStateHandle) {

    private val authRepository = AuthRepository(context, firebaseAuth)
    private val biodataRepository = BiodataRepository(context, database)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                error = "No user logged in"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            userName = currentUser.displayName ?: "User",
            isLoading = true
        )

        // Observe user's biodatas from Room
        viewModelScope.launch {
            biodataRepository.getUserBiodatas(currentUser.uid).collect { biodatas ->
                _uiState.value = _uiState.value.copy(
                    biodatas = biodatas,
                    isLoading = false
                )
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

package com.biodataai.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.biodataai.app.core.Result
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.repository.AuthRepository
import com.biodataai.app.repository.BiodataRepository
import com.biodataai.app.ui.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BiodataCreateUiState(
    val selectedTemplate: String? = null,
    val selectedLanguage: String = "EN",
    val isCreating: Boolean = false,
    val error: String? = null,
    val createdBiodataId: String? = null
)

class BiodataCreateViewModel(
    context: Context,
    firebaseAuth: FirebaseAuth,
    database: BioDataDatabase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<BiodataCreateUiState>(savedStateHandle) {

    private val authRepository = AuthRepository(context, firebaseAuth)
    private val biodataRepository = BiodataRepository(context, database)

    private val _uiState = MutableStateFlow(
        savedStateHandle.get<BiodataCreateUiState>("uiState") ?: BiodataCreateUiState()
    )
    val uiState: StateFlow<BiodataCreateUiState> = _uiState.asStateFlow()

    fun selectTemplate(templateId: String) {
        _uiState.value = _uiState.value.copy(selectedTemplate = templateId)
        savedStateHandle["uiState"] = _uiState.value
    }

    fun selectLanguage(language: String) {
        _uiState.value = _uiState.value.copy(selectedLanguage = language)
        savedStateHandle["uiState"] = _uiState.value
    }

    fun createBiodata() {
        val template = _uiState.value.selectedTemplate
        if (template == null) {
            _uiState.value = _uiState.value.copy(error = "Please select a template")
            return
        }

        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(error = "No user logged in")
            return
        }

        launchAsync {
            _uiState.value = _uiState.value.copy(isCreating = true, error = null)
            when (val result = biodataRepository.createBiodata(
                currentUser.uid,
                template,
                _uiState.value.selectedLanguage
            )) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        createdBiodataId = result.data.id
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        error = result.message ?: "Failed to create biodata"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

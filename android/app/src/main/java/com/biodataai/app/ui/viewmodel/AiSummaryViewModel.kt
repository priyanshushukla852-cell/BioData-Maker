package com.biodataai.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.network.RetrofitClient
import com.biodataai.app.network.api.AiSummaryRequest
import com.biodataai.app.network.api.BiodataService
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class AiSummaryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val summary: String = "",
    val isManualEntry: Boolean = false,
    val keywords: List<String> = emptyList()
)

class AiSummaryViewModel(
    context: Context,
    private val biodataId: String,
    firebaseAuth: FirebaseAuth,
    private val database: BioDataDatabase
) : ViewModel() {

    private val biodataService = RetrofitClient.getRetrofit(context).create(BiodataService::class.java)

    private val _uiState = MutableStateFlow(AiSummaryUiState())
    val uiState: StateFlow<AiSummaryUiState> = _uiState.asStateFlow()

    init {
        generateAiSummary()
    }

    fun generateAiSummary() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val biodata = database.biodataDao().getBiodataById(biodataId)
                if (biodata == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Biodata not found"
                    )
                    return@launch
                }

                val formDataJson = biodata.formDataJson ?: "{}"
                val languagePref = "EN" // TODO: Get from user preference

                val request = AiSummaryRequest(
                    formDataJson = formDataJson,
                    languagePref = languagePref
                )

                // Call AI summary endpoint with 3-second timeout per CLAUDE.md
                val result = withTimeoutOrNull(3000) {
                    biodataService.generateAiSummary(request)
                }

                if (result != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        summary = result.summary,
                        keywords = result.keywords,
                        isManualEntry = false
                    )
                } else {
                    // Timeout: offer manual entry
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "AI summary generation timed out. You can write your own summary below.",
                        isManualEntry = true
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Unable to generate summary. Please write your own.",
                    isManualEntry = true
                )
            }
        }
    }

    fun updateSummary(newSummary: String) {
        _uiState.value = _uiState.value.copy(summary = newSummary)
    }

    fun skipAiAndWriteManually() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isManualEntry = true,
            summary = "",
            error = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

package com.biodataai.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.repository.BiodataRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

data class FormStepUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentStep: Int = 1,
    val formState: FormState = FormState(),
    val isSaving: Boolean = false
)

/**
 * Manages 7-step form state with Room persistence.
 * Saves after each step; syncs to backend non-blocking.
 */
class FormStepViewModel(
    context: Context,
    private val biodataId: String,
    firebaseAuth: FirebaseAuth,
    database: BioDataDatabase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val firebaseAuth = firebaseAuth
    private val biodataRepository = BiodataRepository(context, database)

    private val _uiState = MutableStateFlow(FormStepUiState())
    val uiState: StateFlow<FormStepUiState> = _uiState.asStateFlow()

    init {
        loadExistingBiodata()
    }

    private fun loadExistingBiodata() {
        viewModelScope.launch {
            try {
                val biodata = biodataRepository.getBiodata(biodataId)
                if (biodata is com.biodataai.app.core.Result.Success) {
                    // TODO: Deserialize saved form state from biodata entity
                    // For now, start with empty form
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load biodata: ${e.message}"
                )
            }
        }
    }

    fun updateStep1(step1: PersonalDetailsForm) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(step1 = step1)
        )
    }

    fun updateStep2(step2: FamilyDetailsForm) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(step2 = step2)
        )
    }

    fun updateStep3(step3: EducationCareerForm) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(step3 = step3)
        )
    }

    fun updateStep4(step4: LifestyleForm) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(step4 = step4)
        )
    }

    fun updateStep5(step5: AstrologyForm) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(step5 = step5)
        )
    }

    fun updateStep6(step6: ContactInfoForm) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(step6 = step6)
        )
    }

    fun nextStep() {
        val currentStep = _uiState.value.currentStep
        if (currentStep < 7) {
            saveCurrentStep()
            _uiState.value = _uiState.value.copy(currentStep = currentStep + 1)
        }
    }

    fun previousStep() {
        val currentStep = _uiState.value.currentStep
        if (currentStep > 1) {
            saveCurrentStep()
            _uiState.value = _uiState.value.copy(currentStep = currentStep - 1)
        }
    }

    fun saveCurrentStep() {
        _uiState.value = _uiState.value.copy(isSaving = true)
        viewModelScope.launch {
            try {
                val formState = _uiState.value.formState
                val formJson = Gson().toJson(formState)

                // Save to Room (source of truth)
                val biodata = biodataRepository.getBiodata(biodataId)
                if (biodata is com.biodataai.app.core.Result.Success) {
                    val updated = biodata.data.copy(
                        formDataJson = formJson,
                        updatedAt = Instant.now()
                    )
                    biodataRepository.updateBiodata(updated)
                }

                // Attempt backend sync (non-blocking — continue even if offline)
                syncToBackend()

                _uiState.value = _uiState.value.copy(isSaving = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save: ${e.message}",
                    isSaving = false
                )
            }
        }
    }

    private fun syncToBackend() {
        viewModelScope.launch {
            try {
                val formState = _uiState.value.formState
                // TODO: POST formState to backend /api/biodatas/{id}/form or similar
                // For now, just log success
            } catch (e: Exception) {
                // Log but don't block — offline is OK
            }
        }
    }

    fun completeForm() {
        saveCurrentStep()
        viewModelScope.launch {
            try {
                // TODO: Mark biodata as ready for next step (AI summary/template picker)
                // Update biodata status to COMPLETED or similar
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to complete form: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

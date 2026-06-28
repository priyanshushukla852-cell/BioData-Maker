package com.biodataai.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.db.entity.BiodataEntity
import com.biodataai.app.db.entity.BiodataStatus
import com.biodataai.app.db.entity.LanguagePref
import com.biodataai.app.repository.BiodataRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.util.UUID

data class FormStepUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val formState: FormState = FormState(),
    val isSaving: Boolean = false
)

/**
 * Manages 7-step form state with Room persistence.
 * Saves after each step; syncs to backend non-blocking.
 * Survives configuration change via SavedStateHandle.
 */
class FormStepViewModel(
    context: Context,
    private val biodataId: String,
    firebaseAuth: FirebaseAuth,
    database: BioDataDatabase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val firebaseAuth = firebaseAuth
    private val biodataRepository = BiodataRepository(context, database)
    private val saveMutex = Mutex() // Prevent concurrent saves

    private val _uiState = MutableStateFlow(FormStepUiState())
    val uiState: StateFlow<FormStepUiState> = _uiState.asStateFlow()

    init {
        // Restore state from SavedStateHandle (survives configuration change). Only the
        // form data is persisted, as a JSON string — FormStepUiState isn't Parcelable, and
        // writing it directly throws "Can't put value with type ... into saved state".
        val savedJson = savedStateHandle.get<String>("formStateJson")
        if (savedJson != null) {
            _uiState.value = FormStepUiState(formState = Gson().fromJson(savedJson, FormState::class.java))
        } else {
            loadExistingBiodata()
        }
    }

    override fun onCleared() {
        // Save state to SavedStateHandle (survives process death).
        // No super.onCleared() call: ViewModel's implementation is empty (lint: EmptySuperCall).
        savedStateHandle["formStateJson"] = Gson().toJson(_uiState.value.formState)
    }

    private fun loadExistingBiodata() {
        viewModelScope.launch {
            try {
                val biodata = biodataRepository.getBiodata(biodataId)
                if (biodata is com.biodataai.app.core.Result.Success) {
                    val entity = biodata.data
                    // Deserialize saved form state from Room (source of truth)
                    try {
                        val formState = if (!entity.formDataJson.isNullOrEmpty()) {
                            Gson().fromJson(entity.formDataJson, FormState::class.java)
                        } else {
                            FormState()
                        }
                        _uiState.value = _uiState.value.copy(formState = formState)
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(
                            error = "Corrupted form data. Starting fresh.",
                            formState = FormState()
                        )
                    }
                } else if (biodata is com.biodataai.app.core.Result.Error) {
                    _uiState.value = _uiState.value.copy(
                        error = "Biodata not found"
                    )
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

    fun updateStep7(step7: com.biodataai.app.ui.viewmodel.PhotosForm) {
        require(step7.photoUrls.size <= 5) { "Maximum 5 photos allowed" }
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(step7 = step7)
        )
    }

    fun saveCurrentStep() {
        _uiState.value = _uiState.value.copy(isSaving = true)
        viewModelScope.launch {
            saveMutex.withLock {
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
                        error = "Unable to save form. Please check your connection and try again.",
                        isSaving = false
                    )
                }
            }
        }
    }

    private fun syncToBackend() {
        // Backend sync is performed by BiodataRepository.updateBiodata (invoked from
        // saveCurrentStep), which maps the FormState into the backend's structured sections.
        // Offline edits are retried later by BiodataSyncWorker. Kept as a no-op seam in case a
        // step needs to trigger an out-of-band sync in future.
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

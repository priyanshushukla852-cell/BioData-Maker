package com.biodataai.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biodataai.app.db.BioDataDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TemplateOption(
    val id: String,
    val name: String,
    val description: String,
    val thumbnailUrl: String? = null
)

data class TemplatePickerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val templates: List<TemplateOption> = emptyList(),
    val selectedTemplateId: String? = null
)

class TemplatePickerViewModel(
    context: Context,
    private val biodataId: String,
    firebaseAuth: FirebaseAuth,
    private val database: BioDataDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplatePickerUiState())
    val uiState: StateFlow<TemplatePickerUiState> = _uiState.asStateFlow()

    init {
        loadTemplates()
    }

    private fun loadTemplates() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                // Hardcoded templates for MVP (v1.0)
                // Backend would serve these from /api/templates endpoint
                val templates = listOf(
                    TemplateOption(
                        id = "classic",
                        name = "Classic",
                        description = "Traditional wedding biodata format"
                    ),
                    TemplateOption(
                        id = "modern",
                        name = "Modern",
                        description = "Contemporary professional layout"
                    ),
                    TemplateOption(
                        id = "minimal",
                        name = "Minimal",
                        description = "Clean and simple design"
                    )
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    templates = templates,
                    selectedTemplateId = templates.firstOrNull()?.id
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Unable to load templates"
                )
            }
        }
    }

    fun selectTemplate(templateId: String) {
        _uiState.value = _uiState.value.copy(selectedTemplateId = templateId)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

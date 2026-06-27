package com.biodataai.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.repository.TemplateRepository
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
    private val database: BioDataDatabase,
    private val templateRepository: TemplateRepository = TemplateRepository(context)
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplatePickerUiState())
    val uiState: StateFlow<TemplatePickerUiState> = _uiState.asStateFlow()

    init {
        loadTemplates()
    }

    private fun loadTemplates() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            // Fetches the non-premium catalogue from /api/templates; falls back to the
            // built-in defaults when offline. See TemplateRepository for the v1 scoping rules.
            val templates = templateRepository.getTemplates()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                templates = templates,
                selectedTemplateId = templates.firstOrNull()?.id
            )
        }
    }

    fun selectTemplate(templateId: String) {
        _uiState.value = _uiState.value.copy(selectedTemplateId = templateId)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

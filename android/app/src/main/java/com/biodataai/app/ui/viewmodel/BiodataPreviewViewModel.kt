package com.biodataai.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.template.BiodataDocument
import com.biodataai.app.template.TemplateBlock
import com.biodataai.app.template.TemplateLabels
import com.biodataai.app.template.TemplateRenderer
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BiodataPreviewUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val title: String = "",
    val blocks: List<TemplateBlock> = emptyList(),
    val templateId: String = "",
    val summary: String = ""
)

class BiodataPreviewViewModel(
    context: Context,
    private val biodataId: String,
    private val templateId: String,
    private val summary: String,
    firebaseAuth: FirebaseAuth,
    private val database: BioDataDatabase
) : ViewModel() {

    private val appContext = context.applicationContext

    private val _uiState = MutableStateFlow(BiodataPreviewUiState())
    val uiState: StateFlow<BiodataPreviewUiState> = _uiState.asStateFlow()

    init {
        generatePreview()
    }

    private fun generatePreview() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val biodata = database.biodataDao().getBiodataById(biodataId)
                if (biodata == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Biodata not found")
                    return@launch
                }

                val formState = Gson().fromJson(biodata.formDataJson ?: "{}", FormState::class.java)
                    ?: FormState()
                val labels = TemplateLabels.forLanguage(appContext, biodata.language)
                val document: BiodataDocument =
                    TemplateRenderer.buildDocument(templateId, formState, labels, summary)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    title = document.title,
                    blocks = document.blocks,
                    templateId = templateId,
                    summary = summary
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Unable to generate preview")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

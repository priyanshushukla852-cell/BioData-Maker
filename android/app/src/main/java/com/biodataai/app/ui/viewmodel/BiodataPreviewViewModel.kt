package com.biodataai.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.db.entity.LanguagePref
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
    val language: LanguagePref = LanguagePref.EN,
    val summary: String = ""
)

class BiodataPreviewViewModel(
    context: Context,
    private val biodataId: String,
    private val templateId: String,
    firebaseAuth: FirebaseAuth,
    private val database: BioDataDatabase
) : ViewModel() {

    private val appContext = context.applicationContext

    private val _uiState = MutableStateFlow(BiodataPreviewUiState())
    val uiState: StateFlow<BiodataPreviewUiState> = _uiState.asStateFlow()

    // Set false once we've defaulted the preview to the biodata's own language.
    private var languageInitialized = false

    init {
        generatePreview()
    }

    /** Switch the previewed language (renders that language's labels + stored summary). */
    fun setLanguage(language: LanguagePref) {
        languageInitialized = true
        _uiState.value = _uiState.value.copy(language = language)
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

                // Default to the biodata's own language on first load.
                val language = if (languageInitialized) _uiState.value.language else biodata.language
                val summary = when (language) {
                    LanguagePref.HI -> biodata.summaryHi
                    LanguagePref.EN -> biodata.summaryEn
                } ?: ""

                val formState = Gson().fromJson(biodata.formDataJson ?: "{}", FormState::class.java)
                    ?: FormState()
                val labels = TemplateLabels.forLanguage(appContext, language)
                val document: BiodataDocument =
                    TemplateRenderer.buildDocument(templateId, formState, labels, summary)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    title = document.title,
                    blocks = document.blocks,
                    templateId = templateId,
                    language = language,
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

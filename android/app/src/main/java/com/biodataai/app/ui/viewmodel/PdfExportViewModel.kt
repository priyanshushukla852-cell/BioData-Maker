package com.biodataai.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.template.PdfLayoutEngine
import com.biodataai.app.template.TemplateLabels
import com.biodataai.app.template.TemplateRenderer
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PdfExportUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isExportSuccessful: Boolean = false,
    val pdfFilePath: String? = null,
    val exportProgress: String = "Ready to export"
)

class PdfExportViewModel(
    context: Context,
    private val biodataId: String,
    private val templateId: String,
    firebaseAuth: FirebaseAuth,
    private val database: BioDataDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PdfExportUiState())
    val uiState: StateFlow<PdfExportUiState> = _uiState.asStateFlow()

    private val applicationContext = context.applicationContext

    /**
     * Renders the biodata to a PDF using the shared [TemplateRenderer] + [PdfLayoutEngine] (the same
     * blocks the preview shows). [summary] is the AI/manual "About" text; pass "" if none.
     */
    fun exportPdf(summary: String = "") {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, exportProgress = "Generating PDF...")
        viewModelScope.launch {
            try {
                val biodata = database.biodataDao().getBiodataById(biodataId)
                if (biodata == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Biodata not found")
                    return@launch
                }

                val formState = Gson().fromJson(biodata.formDataJson ?: "{}", FormState::class.java)
                    ?: FormState()
                val biodataName = formState.step1.fullName.ifBlank { "Biodata" }
                val labels = TemplateLabels.forLanguage(applicationContext, biodata.language)
                val document = TemplateRenderer.buildDocument(templateId, formState, labels, summary)

                _uiState.value = _uiState.value.copy(exportProgress = "Creating PDF document...")

                // PdfDocument rendering does file I/O — keep it off the main thread.
                val file = withContext(Dispatchers.IO) {
                    PdfLayoutEngine(applicationContext)
                        .render(document, "Biodata_${biodataName.replace(" ", "_")}")
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isExportSuccessful = true,
                    pdfFilePath = file.absolutePath,
                    exportProgress = "PDF exported successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "PDF export failed. Please try again.",
                    exportProgress = "Error"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

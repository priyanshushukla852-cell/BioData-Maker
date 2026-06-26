package com.biodataai.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.util.PdfExporter
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    fun exportPdf(previewContent: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, exportProgress = "Generating PDF...")
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

                // Deserialize form data to get biodata name
                val formDataJson = biodata.formDataJson ?: "{}"
                val formState = Gson().fromJson(formDataJson, FormState::class.java)
                val biodataName = formState.step1.fullName.ifEmpty { "Biodata" }

                _uiState.value = _uiState.value.copy(exportProgress = "Creating PDF document...")

                // Export PDF
                val result = PdfExporter.exportToPdf(
                    applicationContext,
                    biodataName,
                    previewContent,
                    templateId
                )

                if (result.success && result.filePath != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isExportSuccessful = true,
                        pdfFilePath = result.filePath,
                        exportProgress = "PDF exported successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.error ?: "Unknown error during PDF export",
                        exportProgress = "Export failed"
                    )
                }
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

package com.biodataai.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biodataai.app.db.BioDataDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BiodataPreviewUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val previewTitle: String = "",
    val previewContent: String = "",
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
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Biodata not found"
                    )
                    return@launch
                }

                // Deserialize form data
                val formDataJson = biodata.formDataJson ?: "{}"
                val formState = Gson().fromJson(formDataJson, FormState::class.java)

                // Format preview based on template
                val previewContent = when (templateId) {
                    "classic" -> generateClassicTemplate(formState, summary)
                    "modern" -> generateModernTemplate(formState, summary)
                    "minimal" -> generateMinimalTemplate(formState, summary)
                    else -> "Template not found"
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    previewTitle = "Biodata Preview - ${templateId.replaceFirstChar { it.uppercase() }}",
                    previewContent = previewContent,
                    templateId = templateId,
                    summary = summary
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Unable to generate preview"
                )
            }
        }
    }

    private fun generateClassicTemplate(formState: FormState, summary: String): String {
        val step1 = formState.step1
        val step6 = formState.step6
        return buildString {
            appendLine("═══════════════════════════════════════")
            appendLine("MARRIAGE BIODATA")
            appendLine("═══════════════════════════════════════")
            appendLine()
            appendLine("NAME: ${step1.fullName}")
            appendLine("DATE OF BIRTH: ${step1.dob}")
            appendLine("GENDER: ${step1.gender}")
            appendLine("RELIGION: ${step1.religion}")
            appendLine("HEIGHT: ${step1.heightCm} cm")
            appendLine()
            appendLine("CONTACT INFORMATION")
            appendLine("─────────────────────")
            appendLine("Phone: ${step6.phone}")
            appendLine("Email: ${step6.email}")
            appendLine("Address: ${step6.address}")
            appendLine()
            appendLine("ABOUT")
            appendLine("─────────────────────")
            appendLine(summary)
            appendLine()
        }
    }

    private fun generateModernTemplate(formState: FormState, summary: String): String {
        val step1 = formState.step1
        val step3 = formState.step3
        return buildString {
            appendLine("${step1.fullName.uppercase()}")
            appendLine()
            appendLine("PROFESSIONAL PROFILE")
            appendLine("Occupation: ${step3.occupation}")
            appendLine("Education: ${step3.educationLevel}")
            appendLine("Company: ${step3.companyName}")
            appendLine()
            appendLine("PERSONAL")
            appendLine("Age: ~${calculateAge(step1.dob)}")
            appendLine("Height: ${step1.heightCm} cm")
            appendLine("Religion: ${step1.religion}")
            appendLine()
            appendLine("PROFILE SUMMARY")
            appendLine(summary)
        }
    }

    private fun generateMinimalTemplate(formState: FormState, summary: String): String {
        val step1 = formState.step1
        return buildString {
            appendLine("${step1.fullName}")
            appendLine("${step1.dob} • ${step1.gender}")
            appendLine()
            appendLine(summary)
        }
    }

    private fun calculateAge(dob: String): Int {
        // Simple calculation: assume YYYY-MM-DD format
        return try {
            val birth = dob.split("-")[0].toInt()
            2026 - birth
        } catch (e: Exception) {
            0
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

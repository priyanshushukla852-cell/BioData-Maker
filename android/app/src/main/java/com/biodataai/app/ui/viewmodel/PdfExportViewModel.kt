package com.biodataai.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biodataai.app.core.Result
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.db.entity.LanguagePref
import com.biodataai.app.network.RetrofitClient
import com.biodataai.app.network.api.AiSummaryRequest
import com.biodataai.app.network.api.BiodataService
import com.biodataai.app.repository.BiodataRepository
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
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.HttpException

data class ExportedPdf(val language: LanguagePref, val path: String)

data class PdfExportUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isExportSuccessful: Boolean = false,
    val exportedFiles: List<ExportedPdf> = emptyList(),
    val exportProgress: String = "Ready to export",
    // True if the daily AI cap was hit while generating a missing-language summary.
    val quotaExceeded: Boolean = false,
    // The biodata's own language, used to default the export selection.
    val defaultLanguage: LanguagePref = LanguagePref.EN
)

class PdfExportViewModel(
    context: Context,
    private val biodataId: String,
    private val templateId: String,
    firebaseAuth: FirebaseAuth,
    private val database: BioDataDatabase
) : ViewModel() {

    private val applicationContext = context.applicationContext
    private val biodataService = RetrofitClient.getRetrofit(context).create(BiodataService::class.java)
    private val biodataRepository = BiodataRepository(context, database)

    private val _uiState = MutableStateFlow(PdfExportUiState())
    val uiState: StateFlow<PdfExportUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            database.biodataDao().getBiodataById(biodataId)?.let {
                _uiState.value = _uiState.value.copy(defaultLanguage = it.language)
            }
        }
    }

    /**
     * Renders and saves one PDF per requested language. Each language uses its own localized
     * template labels and its own "About" summary; a language whose summary isn't cached yet is
     * generated via the AI endpoint (which costs one daily AI credit — so exporting both languages
     * costs two). The cached summaries are reused on re-export so the user isn't charged twice.
     */
    fun exportPdf(languages: List<LanguagePref>) {
        if (languages.isEmpty()) return
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null,
            quotaExceeded = false,
            isExportSuccessful = false,
            exportedFiles = emptyList(),
            exportProgress = "Preparing..."
        )
        viewModelScope.launch {
            try {
                val exported = mutableListOf<ExportedPdf>()
                for (language in languages) {
                    val biodata = database.biodataDao().getBiodataById(biodataId)
                    if (biodata == null) {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Biodata not found")
                        return@launch
                    }

                    val cached = if (language == LanguagePref.HI) biodata.summaryHi else biodata.summaryEn
                    val summary = if (!cached.isNullOrBlank()) {
                        cached
                    } else {
                        when (val generated = generateSummary(language)) {
                            is SummaryResult.Ok -> generated.text
                            SummaryResult.QuotaExceeded -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    quotaExceeded = true,
                                    error = "Daily AI limit reached. Watch an ad on the summary " +
                                        "screen to generate the ${languageName(language)} summary.",
                                    exportedFiles = exported
                                )
                                return@launch
                            }
                            SummaryResult.Failed -> "" // proceed without an About paragraph
                        }
                    }

                    val formState = Gson().fromJson(biodata.formDataJson ?: "{}", FormState::class.java)
                        ?: FormState()
                    val biodataName = formState.step1.fullName.ifBlank { "Biodata" }
                    val labels = TemplateLabels.forLanguage(applicationContext, language)
                    val document = TemplateRenderer.buildDocument(templateId, formState, labels, summary)

                    _uiState.value = _uiState.value.copy(
                        exportProgress = "Creating ${languageName(language)} PDF..."
                    )
                    val file = withContext(Dispatchers.IO) {
                        PdfLayoutEngine(applicationContext)
                            .render(document, "Biodata_${biodataName.replace(" ", "_")}_${language.name}")
                    }
                    exported.add(ExportedPdf(language, file.absolutePath))
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isExportSuccessful = true,
                    exportedFiles = exported,
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

    private sealed interface SummaryResult {
        data class Ok(val text: String) : SummaryResult
        data object QuotaExceeded : SummaryResult
        data object Failed : SummaryResult
    }

    /** Generates the summary for [language] via the backend, persists it to Room, and returns it. */
    private suspend fun generateSummary(language: LanguagePref): SummaryResult {
        // The AI endpoint resolves the biodata server-side, so make sure it's synced first.
        if (biodataRepository.ensureSyncedToServer(biodataId) is Result.Error) {
            return SummaryResult.Failed
        }
        _uiState.value = _uiState.value.copy(exportProgress = "Generating ${languageName(language)} summary...")
        return try {
            val response = withTimeoutOrNull(SUMMARY_TIMEOUT_MS) {
                biodataService.generateAiSummary(AiSummaryRequest(biodataId, language.name))
            } ?: return SummaryResult.Failed

            // Persist into the right language slot, preserving the other.
            val biodata = database.biodataDao().getBiodataById(biodataId)
            val en = if (language == LanguagePref.EN) response.summaryText else biodata?.summaryEn
            val hi = if (language == LanguagePref.HI) response.summaryText else biodata?.summaryHi
            database.biodataDao().updateSummaries(biodataId, en, hi)
            SummaryResult.Ok(response.summaryText)
        } catch (e: HttpException) {
            if (e.code() == 429) SummaryResult.QuotaExceeded else SummaryResult.Failed
        } catch (e: Exception) {
            SummaryResult.Failed
        }
    }

    private fun languageName(language: LanguagePref) = if (language == LanguagePref.HI) "Hindi" else "English"

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    companion object {
        private const val SUMMARY_TIMEOUT_MS = 20000L
    }
}

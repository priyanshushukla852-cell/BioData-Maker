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
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.HttpException

data class AiSummaryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val summary: String = "",
    val isManualEntry: Boolean = false,
    // Daily AI cap reached (backend returned 429). When true the UI offers a rewarded ad.
    val quotaExceeded: Boolean = false,
    val adRewardAvailable: Boolean = false
)

class AiSummaryViewModel(
    context: Context,
    private val biodataId: String,
    firebaseAuth: FirebaseAuth,
    private val database: BioDataDatabase
) : ViewModel() {

    private val biodataService = RetrofitClient.getRetrofit(context).create(BiodataService::class.java)
    private val biodataRepository = BiodataRepository(context, database)

    private val _uiState = MutableStateFlow(AiSummaryUiState())
    val uiState: StateFlow<AiSummaryUiState> = _uiState.asStateFlow()

    init {
        generateAiSummary()
    }

    fun generateAiSummary() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, quotaExceeded = false)
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

                // Drafts are offline-first in Room and may not exist on the server yet (or the
                // original create may have failed silently). The AI endpoint resolves the biodata
                // by id server-side, so push it first — otherwise the call 500s on an unknown id and
                // we'd show the misleading "write your own" fallback.
                val sync = biodataRepository.ensureSyncedToServer(biodataId)
                if (sync is Result.Error) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Couldn't sync your biodata. Check your connection and try again.",
                        isManualEntry = true
                    )
                    return@launch
                }

                // biodataId is shared with the server row; language comes from the draft.
                val request = AiSummaryRequest(
                    biodataId = biodataId,
                    language = biodata.language.name
                )

                // Summary generation can take longer than the 3s field-suggestion budget; allow
                // more time here (the backend has its own retry + circuit breaker).
                val result = withTimeoutOrNull(SUMMARY_TIMEOUT_MS) {
                    biodataService.generateAiSummary(request)
                }

                if (result != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        summary = result.summaryText,
                        isManualEntry = false
                    )
                } else {
                    // Timeout: offer manual entry
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "AI summary generation timed out. You can write your own summary below.",
                        isManualEntry = true
                    )
                }
            } catch (e: HttpException) {
                if (e.code() == 429) {
                    // Daily AI cap reached. Offer the rewarded-ad path instead of manual entry.
                    val adAvailable = parseAdRewardAvailable(e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        quotaExceeded = true,
                        adRewardAvailable = adAvailable,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Unable to generate summary. Please write your own.",
                        isManualEntry = true
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Unable to generate summary. Please write your own.",
                    isManualEntry = true
                )
            }
        }
    }

    /**
     * Called after the user finishes a rewarded ad. The backend grant arrives asynchronously via
     * AdMob SSV, so retry with a short delay (and a couple of attempts) to let it land.
     */
    fun retryAfterAdReward() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, quotaExceeded = false, error = null)
            delay(SSV_GRANT_LANDING_DELAY_MS)
            generateAiSummary()
        }
    }

    private fun parseAdRewardAvailable(e: HttpException): Boolean {
        return try {
            val body = e.response()?.errorBody()?.string() ?: return true
            val map = Gson().fromJson(body, Map::class.java)
            map["adRewardAvailable"] as? Boolean ?: true
        } catch (_: Exception) {
            true // default to offering the ad if the body can't be parsed
        }
    }

    fun updateSummary(newSummary: String) {
        _uiState.value = _uiState.value.copy(summary = newSummary)
    }

    /**
     * Persist the current summary into the slot for this biodata's language, then continue. Without
     * this the edited/generated summary was lost on navigation and never reached the PDF.
     */
    fun saveSummaryAndContinue(onSaved: () -> Unit) {
        viewModelScope.launch {
            val biodata = database.biodataDao().getBiodataById(biodataId)
            if (biodata != null) {
                val text = _uiState.value.summary
                val en = if (biodata.language == LanguagePref.EN) text else biodata.summaryEn
                val hi = if (biodata.language == LanguagePref.HI) text else biodata.summaryHi
                database.biodataDao().updateSummaries(biodataId, en, hi)
            }
            onSaved()
        }
    }

    fun skipAiAndWriteManually() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isManualEntry = true,
            summary = "",
            error = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    companion object {
        // AdMob SSV is server-to-server and can land just after the ad is dismissed; wait briefly
        // before retrying so the backend has recorded the +1 grant.
        private const val SSV_GRANT_LANDING_DELAY_MS = 2000L

        // Summary generation budget (field suggestions have the tighter 3s rule, not summaries).
        private const val SUMMARY_TIMEOUT_MS = 20000L
    }
}

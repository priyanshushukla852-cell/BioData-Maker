package com.biodataai.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.network.RetrofitClient
import com.biodataai.app.network.api.AiSummaryRequest
import com.biodataai.app.network.api.BiodataService
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
    val keywords: List<String> = emptyList(),
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

                val formDataJson = biodata.formDataJson ?: "{}"
                val languagePref = "EN" // TODO: Get from user preference

                val request = AiSummaryRequest(
                    formDataJson = formDataJson,
                    languagePref = languagePref
                )

                // Call AI summary endpoint with 3-second timeout per CLAUDE.md
                val result = withTimeoutOrNull(3000) {
                    biodataService.generateAiSummary(request)
                }

                if (result != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        summary = result.summary,
                        keywords = result.keywords,
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
    }
}

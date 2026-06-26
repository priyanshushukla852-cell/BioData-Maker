package com.biodataai.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.biodataai.app.network.NetworkStateManager
import kotlinx.coroutines.flow.StateFlow

data class NetworkConnectivityUiState(
    val isOnline: Boolean = true
)

class NetworkConnectivityViewModel(context: Context) : ViewModel() {
    private val networkStateManager = NetworkStateManager(context)
    
    val isOnline: StateFlow<Boolean> = networkStateManager.isOnline
}

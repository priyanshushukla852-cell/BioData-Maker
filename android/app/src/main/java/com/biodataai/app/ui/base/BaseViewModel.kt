package com.biodataai.app.ui.base

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biodataai.app.core.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<T : Any>(
    protected val savedStateHandle: SavedStateHandle
) : ViewModel() {

    protected fun launchAsync(
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            block()
        }
    }

    protected fun <StateType : Any> createSavedStateFlow(
        key: String,
        initialValue: StateType
    ): StateFlow<StateType> {
        return savedStateHandle.getStateFlow(key, initialValue)
    }

    protected fun <StateType : Any> updateSavedState(key: String, value: StateType) {
        savedStateHandle[key] = value
    }

    protected fun <StateType : Any> getSavedState(key: String): StateType? {
        return savedStateHandle.get(key)
    }
}

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

    protected fun <R> launchAsync(
        block: suspend () -> Result<R>,
        onSuccess: (R) -> Unit = {},
        onError: (Exception, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            when (val result = block()) {
                is Result.Success -> onSuccess(result.data)
                is Result.Error -> onError(result.exception, result.message)
                is Result.Loading -> {}
            }
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

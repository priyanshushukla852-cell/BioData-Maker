package com.biodataai.app.core

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: Exception, val message: String = exception.message ?: "Unknown error") : Result<T>()
    class Loading<T> : Result<T>()

    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(exception, message)
        is Loading -> Loading()
    }

    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (Exception, String) -> Unit): Result<T> {
        if (this is Error) action(exception, message)
        return this
    }

    inline fun <R> fold(
        onSuccess: (T) -> R,
        onError: (Exception, String) -> R,
        onLoading: () -> R = { throw IllegalStateException("Loading state should be handled separately") }
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Error -> onError(exception, message)
        is Loading -> onLoading()
    }

    fun getOrNull(): T? = (this as? Success)?.data
    fun exceptionOrNull(): Exception? = (this as? Error)?.exception
}

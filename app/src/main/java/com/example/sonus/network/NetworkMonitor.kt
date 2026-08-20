package com.example.sonus.network

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object NetworkMonitor {
    private val _networkErrors = MutableSharedFlow<String>(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val networkErrors: SharedFlow<String> = _networkErrors

    fun logError(message: String) {
        _networkErrors.tryEmit(message)
    }
}

package com.biodataai.app.ui.component

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

/**
 * On a root screen, hardware/gesture back otherwise exits the app immediately. This intercepts the
 * first back press to show a footer snackbar (via [snackbarHostState]) and only finishes the
 * activity if a second back arrives within [windowMs].
 *
 * The host screen must render a [androidx.compose.material3.SnackbarHost] bound to the same
 * [snackbarHostState] for the message to appear.
 */
@Composable
fun DoubleBackToExit(
    snackbarHostState: SnackbarHostState,
    message: String,
    enabled: Boolean = true,
    windowMs: Long = 2000L,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Holder so the timestamp survives recomposition without re-arming the handler.
    val lastBackAt = remember { longArrayOf(0L) }

    BackHandler(enabled = enabled) {
        val now = System.currentTimeMillis()
        if (now - lastBackAt[0] < windowMs) {
            (context as? Activity)?.finish()
        } else {
            lastBackAt[0] = now
            scope.launch {
                snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            }
        }
    }
}

package com.biodataai.app.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.biodataai.app.navigation.NavRoute
import com.biodataai.app.ui.viewmodel.SplashViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val firebaseAuth = FirebaseAuth.getInstance()
    // Obtain via a factory so the VM is scoped to the nav entry (survives config change) and we
    // avoid the @VisibleForTesting SavedStateHandle() constructor.
    val viewModel: SplashViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SplashViewModel(firebaseAuth, createSavedStateHandle()) }
        }
    )

    LaunchedEffect(Unit) {
        delay(2000)
        val startRoute = if (viewModel.isUserLoggedIn()) {
            NavRoute.Home
        } else {
            NavRoute.Auth.Login
        }
        navController.navigate(startRoute) {
            popUpTo(NavRoute.Splash) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("BioData AI")
    }
}

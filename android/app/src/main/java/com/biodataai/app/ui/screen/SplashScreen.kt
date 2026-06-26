package com.biodataai.app.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import com.biodataai.app.navigation.NavRoute
import com.biodataai.app.ui.viewmodel.SplashViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val viewModel = SplashViewModel(firebaseAuth, SavedStateHandle())

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

package com.biodataai.app.ui.screen

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import com.biodataai.app.navigation.NavRoute
import com.biodataai.app.ui.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val viewModel = remember {
        LoginViewModel(context, firebaseAuth, SavedStateHandle())
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // Navigate to Home on successful login
    LaunchedEffect(firebaseAuth.currentUser) {
        if (firebaseAuth.currentUser != null) {
            navController.navigate(NavRoute.Home) {
                popUpTo(NavRoute.Auth.Login) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))

        if (uiState.phoneOtpFlow == null) {
            // Login button screen
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text("BioData AI")
                Spacer(Modifier.height(32.dp))
                Text("Create your marriage biodata")
                Spacer(Modifier.height(48.dp))

                Button(
                    onClick = { viewModel.startGoogleSignIn() },
                    modifier = Modifier.padding(16.dp),
                    enabled = !uiState.isLoading
                ) {
                    Text("Continue with Google")
                }

                Button(
                    onClick = { viewModel.startPhoneOtp() },
                    modifier = Modifier.padding(16.dp),
                    enabled = !uiState.isLoading
                ) {
                    Text("Continue with Phone")
                }

                if (uiState.isLoading) {
                    Spacer(Modifier.height(24.dp))
                    CircularProgressIndicator()
                }
            }
        } else {
            // Phone OTP flow
            val flow = uiState.phoneOtpFlow
            if (flow != null) {
                PhoneOtpScreen(
                    flow = flow,
                    isLoading = uiState.isLoading,
                    onPhoneSubmit = { viewModel.submitPhoneNumber(it) },
                    onOtpSubmit = { viewModel.submitOtp(it) },
                    onCancel = { viewModel.cancelPhoneOtp() }
                )
            }
        }
    }

    // Handle Google Sign-In initiation (real app would trigger Google Sign-In intent)
    if (uiState.googleSignInInitiated) {
        // In a real app, this would launch the Google Sign-In flow
        // and get an idToken from GoogleSignInResult
        // For now, we just show a message
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("Google Sign-In not yet configured")
        }
    }
}

@Composable
private fun PhoneOtpScreen(
    flow: com.biodataai.app.ui.viewmodel.PhoneOtpFlow,
    isLoading: Boolean,
    onPhoneSubmit: (String) -> Unit,
    onOtpSubmit: (String) -> Unit,
    onCancel: () -> Unit
) {
    var phoneInput by remember { mutableStateOf(flow.phoneNumber) }
    var otpInput by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Phone Verification")
        Spacer(Modifier.height(24.dp))

        if (flow.step == 1) {
            OutlinedTextField(
                value = phoneInput,
                onValueChange = { phoneInput = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = !isLoading
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onPhoneSubmit(phoneInput) },
                enabled = !isLoading && phoneInput.isNotBlank()
            ) {
                Text("Send OTP")
            }
        } else if (flow.step == 2) {
            Text("Enter OTP sent to ${flow.phoneNumber}")
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = otpInput,
                onValueChange = { otpInput = it },
                label = { Text("OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !isLoading
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onOtpSubmit(otpInput) },
                enabled = !isLoading && otpInput.isNotBlank()
            ) {
                Text("Verify")
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onCancel() },
            enabled = !isLoading
        ) {
            Text("Cancel")
        }

        if (isLoading) {
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator()
        }
    }
}

package com.biodataai.app.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biodataai.app.navigation.NavRoute

@Composable
fun LoginScreen(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Login with Google or Phone")
            Button(
                onClick = { navController.navigate(NavRoute.Home) },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Continue with Google")
            }
            Button(
                onClick = { navController.navigate(NavRoute.Auth.PhoneOtp) },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Continue with Phone")
            }
        }
    }
}

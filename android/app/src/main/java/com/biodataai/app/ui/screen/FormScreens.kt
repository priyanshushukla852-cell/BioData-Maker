package com.biodataai.app.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun BiodataCreateScreen(navController: NavController, biodataId: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Create/Edit Biodata")
    }
}

@Composable
fun FormStepScreen(navController: NavController, biodataId: String, step: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Step $step of 7")
    }
}

@Composable
fun AiSummaryReviewScreen(navController: NavController, biodataId: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("AI Summary Review")
    }
}

@Composable
fun TemplatePickerScreen(navController: NavController, biodataId: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Choose Template")
    }
}

@Composable
fun BiodataPreviewScreen(navController: NavController, biodataId: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Preview Biodata")
    }
}

@Composable
fun PdfExportScreen(navController: NavController, biodataId: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Export PDF")
    }
}

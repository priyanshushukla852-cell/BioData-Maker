package com.biodataai.app.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.navigation.NavRoute
import com.biodataai.app.ui.viewmodel.BiodataCreateViewModel
import com.google.firebase.auth.FirebaseAuth

// Template options available to users
private val AVAILABLE_TEMPLATES = listOf(
    "classic" to "Classic",
    "modern" to "Modern",
    "traditional" to "Traditional"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiodataCreateScreen(navController: NavController, biodataId: String?) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val database = BioDataDatabase.getInstance(context)
    val viewModel = remember {
        BiodataCreateViewModel(context, firebaseAuth, database, SavedStateHandle())
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Navigate to form step 1 on successful creation
    LaunchedEffect(uiState.createdBiodataId) {
        uiState.createdBiodataId?.let { biodataId ->
            navController.navigate(NavRoute.FormStep(biodataId, 1)) {
                popUpTo(NavRoute.BiodataCreate()) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Biodata") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                "Select a template",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))

            // Template selection
            AVAILABLE_TEMPLATES.forEach { (templateId, templateName) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { viewModel.selectTemplate(templateId) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.selectedTemplate == templateId,
                            onClick = { viewModel.selectTemplate(templateId) }
                        )
                        Spacer(Modifier.padding(horizontal = 8.dp))
                        Text(templateName)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Select language",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            // Language selection
            listOf("EN" to "English", "HI" to "हिन्दी").forEach { (lang, langName) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { viewModel.selectLanguage(lang) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.selectedLanguage == lang,
                            onClick = { viewModel.selectLanguage(lang) }
                        )
                        Spacer(Modifier.padding(horizontal = 8.dp))
                        Text(langName)
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = { viewModel.createBiodata() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !uiState.isCreating && uiState.selectedTemplate != null
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp)
                    )
                } else {
                    Text("Create")
                }
            }
        }
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

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biodataai.app.R
import com.biodataai.app.ui.component.DoubleBackToExit
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.db.entity.BiodataEntity
import com.biodataai.app.db.entity.BiodataStatus
import com.biodataai.app.navigation.NavRoute
import com.biodataai.app.ui.component.OfflineStateBanner
import com.biodataai.app.ui.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val firebaseAuth = FirebaseAuth.getInstance()
    val database = BioDataDatabase.getInstance(context)
    val viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer { HomeViewModel(appContext, firebaseAuth, database, createSavedStateHandle()) }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var biodataToDelete by remember { mutableStateOf<BiodataEntity?>(null) }

    // Home is the post-login root: back here would otherwise exit the app outright.
    DoubleBackToExit(snackbarHostState, message = stringResource(R.string.press_back_again_to_exit))

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(NavRoute.BiodataCreate()) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Biodata")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!uiState.isOnline) {
                OfflineStateBanner()
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "My Biodatas",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Hello, ${uiState.userName}",
                        fontSize = 14.sp
                    )
                }
                IconButton(
                    onClick = {
                        viewModel.signOut()
                        navController.navigate(NavRoute.Auth.Login) {
                            popUpTo(NavRoute.Home) { inclusive = true }
                        }
                    }
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
                }
            }

            when {
                uiState.isLoading && uiState.biodatas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.biodatas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("No biodatas yet", fontSize = 18.sp)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Tap the + button to create your first marriage biodata",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(uiState.biodatas) { biodata ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable {
                                        // Drafts reopen in the editable form (loads saved data);
                                        // completed biodatas open the read-only preview.
                                        if (biodata.status == BiodataStatus.DRAFT) {
                                            navController.navigate(NavRoute.FormStep(biodata.id, 1))
                                        } else {
                                            navController.navigate(
                                                NavRoute.BiodataPreview(
                                                    biodata.id,
                                                    biodata.templateId ?: "classic"
                                                )
                                            )
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            biodata.title.ifEmpty { biodata.templateId ?: "Biodata" },
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "Status: ${biodata.status.name}",
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "Language: ${biodata.language.name}",
                                            fontSize = 14.sp
                                        )
                                    }
                                    IconButton(onClick = { biodataToDelete = biodata }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete biodata",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        biodataToDelete?.let { target ->
            AlertDialog(
                onDismissRequest = { biodataToDelete = null },
                title = { Text("Delete biodata?") },
                text = {
                    Text(
                        "This will remove \"${target.title.ifEmpty { "this biodata" }}\". " +
                            "This can't be undone."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteBiodata(target.id)
                        biodataToDelete = null
                    }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { biodataToDelete = null }) { Text("Cancel") }
                }
            )
        }
    }
}

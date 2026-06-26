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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.navigation.NavRoute
import com.biodataai.app.ui.component.FormTextField
import com.biodataai.app.ui.viewmodel.BiodataCreateViewModel
import com.biodataai.app.ui.viewmodel.ContactInfoForm
import com.biodataai.app.ui.viewmodel.EducationCareerForm
import com.biodataai.app.ui.viewmodel.FamilyDetailsForm
import com.biodataai.app.ui.viewmodel.FormStepViewModel
import com.biodataai.app.ui.viewmodel.LifestyleForm
import com.biodataai.app.ui.viewmodel.PersonalDetailsForm
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormStepScreen(navController: NavController, biodataId: String, step: Int) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val database = BioDataDatabase.getInstance(context)
    val viewModel = remember {
        FormStepViewModel(context, biodataId, firebaseAuth, database, SavedStateHandle())
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Step $step of 7") },
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
        ) {
            LinearProgressIndicator(
                progress = step.toFloat() / 7f,
                modifier = Modifier.fillMaxWidth()
            )

            when (step) {
                1 -> FormStep1PersonalDetails(viewModel, uiState)
                2 -> FormStep2FamilyDetails(viewModel, uiState)
                3 -> FormStep3EducationCareer(viewModel, uiState)
                4 -> FormStep4Lifestyle(viewModel, uiState)
                5 -> FormStep5Astrology(viewModel, uiState)
                6 -> FormStep6ContactInfo(viewModel, uiState)
                7 -> FormStep7Photos(viewModel, uiState)
                else -> Text("Unknown step")
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.previousStep() },
                    modifier = Modifier.weight(1f),
                    enabled = step > 1 && !uiState.isSaving
                ) {
                    Text("Back")
                }

                Button(
                    onClick = {
                        if (step == 7) {
                            viewModel.completeForm()
                            navController.navigate(NavRoute.AiSummaryReview(biodataId)) {
                                popUpTo(NavRoute.FormStep(biodataId, 1)) { inclusive = true }
                            }
                        } else {
                            viewModel.nextStep()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.height(20.dp))
                    } else {
                        Text(if (step == 7) "Complete" else "Next")
                    }
                }
            }
        }
    }
}

@Composable
private fun FormStep1PersonalDetails(viewModel: FormStepViewModel, uiState: com.biodataai.app.ui.viewmodel.FormStepUiState) {
    var fullName by remember { mutableStateOf(uiState.formState.step1.fullName) }
    var dob by remember { mutableStateOf(uiState.formState.step1.dob) }
    var gender by remember { mutableStateOf(uiState.formState.step1.gender) }
    var religion by remember { mutableStateOf(uiState.formState.step1.religion) }
    var heightCm by remember { mutableStateOf(uiState.formState.step1.heightCm) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        item {
            FormTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    viewModel.updateStep1(uiState.formState.step1.copy(fullName = it))
                },
                label = "Full Name *",
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            FormTextField(
                value = dob,
                onValueChange = {
                    dob = it
                    viewModel.updateStep1(uiState.formState.step1.copy(dob = it))
                },
                label = "Date of Birth (YYYY-MM-DD) *",
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text("Gender *", fontWeight = FontWeight.Bold)
            Row {
                listOf("MALE", "FEMALE", "OTHER").forEach { g ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            gender = g
                            viewModel.updateStep1(uiState.formState.step1.copy(gender = g))
                        }
                    ) {
                        RadioButton(
                            selected = gender == g,
                            onClick = {
                                gender = g
                                viewModel.updateStep1(uiState.formState.step1.copy(gender = g))
                            }
                        )
                        Text(g.replace("_", " "))
                    }
                }
            }
        }
        item {
            FormTextField(
                value = religion,
                onValueChange = {
                    religion = it
                    viewModel.updateStep1(uiState.formState.step1.copy(religion = it))
                },
                label = "Religion",
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            FormTextField(
                value = heightCm,
                onValueChange = {
                    heightCm = it
                    viewModel.updateStep1(uiState.formState.step1.copy(heightCm = it))
                },
                label = "Height (cm)",
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Number
            )
        }
    }
}

@Composable
private fun FormStep2FamilyDetails(viewModel: FormStepViewModel, uiState: com.biodataai.app.ui.viewmodel.FormStepUiState) {
    var fatherName by remember { mutableStateOf(uiState.formState.step2.fatherName) }
    var motherName by remember { mutableStateOf(uiState.formState.step2.motherName) }
    var siblingsCount by remember { mutableStateOf(uiState.formState.step2.siblingsCount) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        item { FormTextField(value = fatherName, onValueChange = { fatherName = it; viewModel.updateStep2(uiState.formState.step2.copy(fatherName = it)) }, label = "Father's Name", modifier = Modifier.fillMaxWidth()) }
        item { FormTextField(value = motherName, onValueChange = { motherName = it; viewModel.updateStep2(uiState.formState.step2.copy(motherName = it)) }, label = "Mother's Name", modifier = Modifier.fillMaxWidth()) }
        item { FormTextField(value = siblingsCount, onValueChange = { siblingsCount = it; viewModel.updateStep2(uiState.formState.step2.copy(siblingsCount = it)) }, label = "Number of Siblings", modifier = Modifier.fillMaxWidth(), keyboardType = KeyboardType.Number) }
    }
}

@Composable
private fun FormStep3EducationCareer(viewModel: FormStepViewModel, uiState: com.biodataai.app.ui.viewmodel.FormStepUiState) {
    var education by remember { mutableStateOf(uiState.formState.step3.educationLevel) }
    var occupation by remember { mutableStateOf(uiState.formState.step3.occupation) }
    var income by remember { mutableStateOf(uiState.formState.step3.income) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        item { FormTextField(value = education, onValueChange = { education = it; viewModel.updateStep3(uiState.formState.step3.copy(educationLevel = it)) }, label = "Education Level", modifier = Modifier.fillMaxWidth()) }
        item { FormTextField(value = occupation, onValueChange = { occupation = it; viewModel.updateStep3(uiState.formState.step3.copy(occupation = it)) }, label = "Occupation", modifier = Modifier.fillMaxWidth()) }
        item { FormTextField(value = income, onValueChange = { income = it; viewModel.updateStep3(uiState.formState.step3.copy(income = it)) }, label = "Annual Income (Optional)", modifier = Modifier.fillMaxWidth(), keyboardType = KeyboardType.Number) }
    }
}

@Composable
private fun FormStep4Lifestyle(viewModel: FormStepViewModel, uiState: com.biodataai.app.ui.viewmodel.FormStepUiState) {
    var diet by remember { mutableStateOf(uiState.formState.step4.diet) }
    var hobbies by remember { mutableStateOf(uiState.formState.step4.hobbies) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        item { FormTextField(value = diet, onValueChange = { diet = it; viewModel.updateStep4(uiState.formState.step4.copy(diet = it)) }, label = "Diet (VEG/NON_VEG)", modifier = Modifier.fillMaxWidth()) }
        item { FormTextField(value = hobbies, onValueChange = { hobbies = it; viewModel.updateStep4(uiState.formState.step4.copy(hobbies = it)) }, label = "Hobbies & Interests", modifier = Modifier.fillMaxWidth()) }
    }
}

@Composable
private fun FormStep5Astrology(viewModel: FormStepViewModel, uiState: com.biodataai.app.ui.viewmodel.FormStepUiState) {
    var birthPlace by remember { mutableStateOf(uiState.formState.step5.birthPlace) }
    var isManglik by remember { mutableStateOf(uiState.formState.step5.isManglik) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Astrology (Optional)", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        item { FormTextField(value = birthPlace, onValueChange = { birthPlace = it; viewModel.updateStep5(uiState.formState.step5.copy(birthPlace = it)) }, label = "Birth Place", modifier = Modifier.fillMaxWidth()) }
        item { FormTextField(value = isManglik, onValueChange = { isManglik = it; viewModel.updateStep5(uiState.formState.step5.copy(isManglik = it)) }, label = "Manglik Status (Optional)", modifier = Modifier.fillMaxWidth()) }
    }
}

@Composable
private fun FormStep6ContactInfo(viewModel: FormStepViewModel, uiState: com.biodataai.app.ui.viewmodel.FormStepUiState) {
    var phone by remember { mutableStateOf(uiState.formState.step6.phone) }
    var email by remember { mutableStateOf(uiState.formState.step6.email) }
    var address by remember { mutableStateOf(uiState.formState.step6.address) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        item { FormTextField(value = phone, onValueChange = { phone = it; viewModel.updateStep6(uiState.formState.step6.copy(phone = it)) }, label = "Phone *", modifier = Modifier.fillMaxWidth(), keyboardType = KeyboardType.Phone) }
        item { FormTextField(value = email, onValueChange = { email = it; viewModel.updateStep6(uiState.formState.step6.copy(email = it)) }, label = "Email *", modifier = Modifier.fillMaxWidth(), keyboardType = KeyboardType.Email) }
        item { FormTextField(value = address, onValueChange = { address = it; viewModel.updateStep6(uiState.formState.step6.copy(address = it)) }, label = "Address *", modifier = Modifier.fillMaxWidth()) }
    }
}

@Composable
private fun FormStep7Photos(viewModel: FormStepViewModel, uiState: com.biodataai.app.ui.viewmodel.FormStepUiState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Photo upload coming in next iteration", fontSize = 14.sp)
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

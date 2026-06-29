package com.biodataai.app.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
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
import com.biodataai.app.ui.viewmodel.PhotosForm
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import com.biodataai.app.util.AdManager
import com.biodataai.app.util.ImageCompressor
import com.biodataai.app.util.InputValidation
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.biodataai.app.R
import java.io.File

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
    val appContext = context.applicationContext
    val firebaseAuth = FirebaseAuth.getInstance()
    val database = BioDataDatabase.getInstance(context)
    val viewModel: BiodataCreateViewModel = viewModel(
        factory = viewModelFactory {
            initializer { BiodataCreateViewModel(appContext, firebaseAuth, database, createSavedStateHandle()) }
        }
    )

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
                title = { Text(stringResource(R.string.create_biodata)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = stringResource(R.string.back))
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
                stringResource(R.string.select_template),
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
    val appContext = context.applicationContext
    val firebaseAuth = FirebaseAuth.getInstance()
    val database = BioDataDatabase.getInstance(context)
    val viewModel: FormStepViewModel = viewModel(
        factory = viewModelFactory {
            initializer { FormStepViewModel(appContext, biodataId, firebaseAuth, database, createSavedStateHandle()) }
        }
    )

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
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = stringResource(R.string.back))
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
                // Inset the content by the keyboard height so the focused field isn't hidden;
                // the step's LazyColumn then auto-scrolls the focused field into the smaller viewport.
                .imePadding()
        ) {
            LinearProgressIndicator(
                progress = { step.toFloat() / 7f },
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

            // Step 6 (contact) requires a validly-formatted phone and email before advancing. Step
            // composables write to formState on each keystroke, so this stays in sync. Other steps
            // are unrestricted.
            val canProceed = when (step) {
                6 -> {
                    val s6 = uiState.formState.step6
                    InputValidation.isValidPhone(s6.phone) && InputValidation.isValidEmail(s6.email)
                }
                else -> true
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.saveCurrentStep()
                        navController.popBackStack()
                    },
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
                            viewModel.saveCurrentStep()
                            navController.navigate(NavRoute.FormStep(biodataId, step + 1))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isSaving && canProceed
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

/**
 * Single-choice chip row for categorical fields — faster (one tap) and less error-prone than
 * typing, which matters on this long form. [options] is value-to-display; the stored value is the
 * first element (must match the backend's expected token), the second is what the user sees.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FormSingleChoice(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column {
        Text(label, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        FlowRow(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
            options.forEach { (value, display) ->
                FilterChip(
                    selected = selected == value,
                    onClick = { onSelect(value) },
                    label = { Text(display) }
                )
            }
        }
    }
}

/**
 * Editable, searchable dropdown for medium-sized lists (e.g. religion). The text field accepts
 * free input — what the user types is the value, so custom entries are allowed — while typing
 * filters [options] (case-insensitive substring) into a shortlist they can tap to fill in.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormSearchableDropdown(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val filtered = if (value.isBlank()) {
        options
    } else {
        options.filter { it.contains(value.trim(), ignoreCase = true) }
    }
    ExposedDropdownMenuBox(
        expanded = expanded && filtered.isNotEmpty(),
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded && filtered.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            filtered.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FormStep1PersonalDetails(viewModel: FormStepViewModel, uiState: com.biodataai.app.ui.viewmodel.FormStepUiState) {
    var fullName by remember { mutableStateOf(uiState.formState.step1.fullName) }
    // Field shows DD-MM-YYYY (user-friendly); FormState keeps ISO yyyy-MM-dd (or "" while partial).
    // Held as TextFieldValue so the caret can be repositioned after auto-inserting dashes — a plain
    // String field can't track the caret across the reformat, making it jump left mid-typing.
    var dobField by remember {
        mutableStateOf(
            InputValidation.isoToDobDisplay(uiState.formState.step1.dob).let {
                TextFieldValue(it, TextRange(it.length))
            }
        )
    }
    var gender by remember { mutableStateOf(uiState.formState.step1.gender) }
    var religion by remember { mutableStateOf(uiState.formState.step1.religion) }
    var heightCm by remember { mutableStateOf(uiState.formState.step1.heightCm) }
    var bloodGroup by remember { mutableStateOf(uiState.formState.step1.bloodGroup) }
    var disability by remember { mutableStateOf(uiState.formState.step1.disability) }

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
            val dobError = dobField.text.isNotBlank() &&
                InputValidation.dobDisplayToIso(dobField.text) == null
            OutlinedTextField(
                value = dobField,
                onValueChange = { input ->
                    // Auto-insert dashes as the user types; skip the trailing dash on deletion so
                    // backspace isn't blocked by a just-completed segment's separator.
                    val deleting = input.text.length < dobField.text.length
                    val formatted = InputValidation.formatDobInput(input.text, allowTrailingDash = !deleting)
                    // Keep the caret after the same number of digits the user had typed before it,
                    // so an injected '-' doesn't shove the cursor (and following digits) left.
                    val digitsBeforeCaret = input.text.take(input.selection.end).count(Char::isDigit)
                    val caret = InputValidation.dobCaretForDigitCount(formatted, digitsBeforeCaret)
                    dobField = TextFieldValue(formatted, TextRange(caret))
                    // Persist ISO only once it's a complete valid date; partial input stays "".
                    val iso = InputValidation.dobDisplayToIso(formatted)
                    viewModel.updateStep1(uiState.formState.step1.copy(dob = iso ?: ""))
                },
                label = { Text("Date of Birth (DD-MM-YYYY) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = dobError,
                supportingText = if (dobError) {
                    { Text(stringResource(R.string.invalid_date_format)) }
                } else null
            )
        }
        item {
            FormSingleChoice(
                label = "Gender *",
                options = listOf("MALE" to "Male", "FEMALE" to "Female", "OTHER" to "Other"),
                selected = gender,
                onSelect = { gender = it; viewModel.updateStep1(uiState.formState.step1.copy(gender = it)) }
            )
        }
        item {
            FormSearchableDropdown(
                label = "Religion",
                value = religion,
                // Hindu first (most common in this app's audience); free text still allowed.
                options = listOf(
                    "Hindu", "Muslim", "Christian", "Sikh", "Jain", "Buddhist", "Parsi", "Jewish", "Other"
                ),
                onValueChange = {
                    religion = it
                    viewModel.updateStep1(uiState.formState.step1.copy(religion = it))
                },
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
        item {
            FormSingleChoice(
                label = "Blood Group",
                options = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-").map { it to it },
                selected = bloodGroup,
                onSelect = { bloodGroup = it; viewModel.updateStep1(uiState.formState.step1.copy(bloodGroup = it)) }
            )
        }
        item {
            FormTextField(
                value = disability,
                onValueChange = {
                    disability = it
                    viewModel.updateStep1(uiState.formState.step1.copy(disability = it))
                },
                label = "Disability (if any)",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FormStep2FamilyDetails(viewModel: FormStepViewModel, uiState: com.biodataai.app.ui.viewmodel.FormStepUiState) {
    var fatherName by remember { mutableStateOf(uiState.formState.step2.fatherName) }
    var motherName by remember { mutableStateOf(uiState.formState.step2.motherName) }
    var siblingsCount by remember { mutableStateOf(uiState.formState.step2.siblingsCount) }
    var familyValues by remember { mutableStateOf(uiState.formState.step2.familyValues) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        item { FormTextField(value = fatherName, onValueChange = { fatherName = it; viewModel.updateStep2(uiState.formState.step2.copy(fatherName = it)) }, label = "Father's Name", modifier = Modifier.fillMaxWidth()) }
        item { FormTextField(value = motherName, onValueChange = { motherName = it; viewModel.updateStep2(uiState.formState.step2.copy(motherName = it)) }, label = "Mother's Name", modifier = Modifier.fillMaxWidth()) }
        item { FormTextField(value = siblingsCount, onValueChange = { siblingsCount = it; viewModel.updateStep2(uiState.formState.step2.copy(siblingsCount = it)) }, label = "Number of Siblings", modifier = Modifier.fillMaxWidth(), keyboardType = KeyboardType.Number) }
        item { FormTextField(value = familyValues, onValueChange = { familyValues = it; viewModel.updateStep2(uiState.formState.step2.copy(familyValues = it)) }, label = "Family Values (e.g. Traditional, Moderate)", modifier = Modifier.fillMaxWidth()) }
    }
}

@Composable
private fun FormStep3EducationCareer(viewModel: FormStepViewModel, uiState: com.biodataai.app.ui.viewmodel.FormStepUiState) {
    var education by remember { mutableStateOf(uiState.formState.step3.educationLevel) }
    var occupation by remember { mutableStateOf(uiState.formState.step3.occupation) }
    var income by remember { mutableStateOf(uiState.formState.step3.income) }
    var college by remember { mutableStateOf(uiState.formState.step3.college) }
    var workLocation by remember { mutableStateOf(uiState.formState.step3.workLocation) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        item { FormTextField(value = education, onValueChange = { education = it; viewModel.updateStep3(uiState.formState.step3.copy(educationLevel = it)) }, label = "Education Level", modifier = Modifier.fillMaxWidth()) }
        item { FormTextField(value = occupation, onValueChange = { occupation = it; viewModel.updateStep3(uiState.formState.step3.copy(occupation = it)) }, label = "Occupation", modifier = Modifier.fillMaxWidth()) }
        item { FormTextField(value = college, onValueChange = { college = it; viewModel.updateStep3(uiState.formState.step3.copy(college = it)) }, label = "College / University", modifier = Modifier.fillMaxWidth()) }
        item { FormTextField(value = workLocation, onValueChange = { workLocation = it; viewModel.updateStep3(uiState.formState.step3.copy(workLocation = it)) }, label = "Work Location", modifier = Modifier.fillMaxWidth()) }
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
        item {
            FormSingleChoice(
                label = "Diet",
                options = listOf("VEG" to "Veg", "NON_VEG" to "Non-Veg"),
                selected = diet,
                onSelect = { diet = it; viewModel.updateStep4(uiState.formState.step4.copy(diet = it)) }
            )
        }
        item { FormTextField(value = hobbies, onValueChange = { hobbies = it; viewModel.updateStep4(uiState.formState.step4.copy(hobbies = it)) }, label = "Hobbies & Interests", placeholder = "What to highlight in your summary", modifier = Modifier.fillMaxWidth()) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormStep5Astrology(viewModel: FormStepViewModel, uiState: com.biodataai.app.ui.viewmodel.FormStepUiState) {
    var birthPlace by remember { mutableStateOf(uiState.formState.step5.birthPlace) }
    var birthTime by remember { mutableStateOf(uiState.formState.step5.birthTime) }
    var isManglik by remember { mutableStateOf(uiState.formState.step5.isManglik) }
    var nakshatra by remember { mutableStateOf(uiState.formState.step5.nakshatra) }
    var showTimePicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Astrology (Optional)", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        item { FormTextField(value = birthPlace, onValueChange = { birthPlace = it; viewModel.updateStep5(uiState.formState.step5.copy(birthPlace = it)) }, label = "Birth Place", modifier = Modifier.fillMaxWidth()) }
        item {
            // A picker (not free text) so the value is always a valid HH:mm — the backend parses
            // it as a time, and a malformed string would otherwise fail the whole sync.
            OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(if (birthTime.isBlank()) "Select Birth Time (Optional)" else "Birth Time: $birthTime")
            }
        }
        item {
            FormSearchableDropdown(
                label = "Nakshatra (Optional)",
                value = nakshatra,
                options = listOf(
                    "Anuradha", "Ardra", "Ashlesha", "Ashwini", "Bharani", "Chitra", "Dhanishta",
                    "Hasta", "Jyeshtha", "Krittika", "Magha", "Mrigashira", "Mula", "Punarvasu",
                    "Purva Ashadha", "Purva Bhadrapada", "Purva Phalguni", "Pushya", "Revati",
                    "Rohini", "Shatabhisha", "Shravana", "Swati", "Uttara Ashadha",
                    "Uttara Bhadrapada", "Uttara Phalguni", "Vishakha"
                ),
                onValueChange = {
                    nakshatra = it
                    viewModel.updateStep5(uiState.formState.step5.copy(nakshatra = it))
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            FormSingleChoice(
                label = "Manglik Status (Optional)",
                options = listOf("YES" to "Yes", "NO" to "No", "PARTIAL" to "Partial"),
                selected = isManglik,
                onSelect = { isManglik = it; viewModel.updateStep5(uiState.formState.step5.copy(isManglik = it)) }
            )
        }
    }

    if (showTimePicker) {
        // Seed from any existing HH:mm value; fall back to noon.
        val parts = birthTime.split(":")
        val initialHour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 12
        val initialMinute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        val timeState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val value = "%02d:%02d".format(timeState.hour, timeState.minute)
                    birthTime = value
                    viewModel.updateStep5(uiState.formState.step5.copy(birthTime = value))
                    showTimePicker = false
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            },
            text = { TimePicker(state = timeState) }
        )
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
        item {
            val phoneError = phone.isNotBlank() && !InputValidation.isValidPhone(phone)
            FormTextField(value = phone, onValueChange = { phone = it; viewModel.updateStep6(uiState.formState.step6.copy(phone = it)) }, label = "Phone *", modifier = Modifier.fillMaxWidth(), keyboardType = KeyboardType.Phone, isError = phoneError, supportingText = if (phoneError) stringResource(R.string.invalid_phone) else null)
        }
        item {
            val emailError = email.isNotBlank() && !InputValidation.isValidEmail(email)
            FormTextField(value = email, onValueChange = { email = it; viewModel.updateStep6(uiState.formState.step6.copy(email = it)) }, label = "Email *", modifier = Modifier.fillMaxWidth(), keyboardType = KeyboardType.Email, isError = emailError, supportingText = if (emailError) stringResource(R.string.invalid_email) else null)
        }
        item { FormTextField(value = address, onValueChange = { address = it; viewModel.updateStep6(uiState.formState.step6.copy(address = it)) }, label = "Address *", modifier = Modifier.fillMaxWidth()) }
    }
}

@Composable
private fun FormStep7Photos(viewModel: FormStepViewModel, uiState: com.biodataai.app.ui.viewmodel.FormStepUiState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCompressing by remember { mutableStateOf(false) }
    var compressionError by remember { mutableStateOf<String?>(null) }
    var photoToDelete by remember { mutableStateOf<String?>(null) }

    // Android Photo Picker: needs no storage permission on any API level (READ_EXTERNAL_STORAGE
    // is denied outright on Android 13+, which is what caused the "permission denied" error).
    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isCompressing = true
            scope.launch {
                try {
                    val result = ImageCompressor.compressImage(context, uri)
                    if (result.success && result.compressedUri != null) {
                        val currentPhotos = uiState.formState.step7.photoUrls.toMutableList()
                        currentPhotos.add(result.compressedUri.toString())
                        viewModel.updateStep7(PhotosForm(currentPhotos))
                        compressionError = null
                    } else {
                        compressionError = result.error ?: "Photo compression failed"
                    }
                } catch (e: Exception) {
                    compressionError = "Unable to process photo. Please try again."
                } finally {
                    isCompressing = false
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(stringResource(R.string.photos_title), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(stringResource(R.string.photos_subtitle), fontSize = 12.sp)
        }

        item {
            Button(
                onClick = {
                    photoLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCompressing && uiState.formState.step7.photoUrls.size < 5
            ) {
                if (isCompressing) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp))
                } else {
                    Text(stringResource(R.string.add_photo_button))
                }
            }
        }

        if (compressionError != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.error_label), fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                        Text(compressionError!!, fontSize = 12.sp)
                    }
                }
            }
        }

        items(uiState.formState.step7.photoUrls.size) { index ->
            val photoUri = uiState.formState.step7.photoUrls[index]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.photo_label, index + 1), fontWeight = FontWeight.Bold)
                        Text(photoUri.takeLast(30), fontSize = 12.sp, maxLines = 1)
                    }
                    IconButton(
                        onClick = { photoToDelete = photoUri }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_photo_description), tint = androidx.compose.material3.MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        item {
            Text(stringResource(R.string.photos_count, uiState.formState.step7.photoUrls.size), fontSize = 12.sp)
        }
    }

    if (photoToDelete != null) {
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            title = { Text(stringResource(R.string.delete_photo_dialog_title)) },
            text = { Text(stringResource(R.string.delete_photo_dialog_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        photoToDelete?.let { uri ->
                            try {
                                val file = File(android.net.Uri.parse(uri).path ?: return@let)
                                if (file.exists()) file.delete()
                            } catch (e: Exception) {
                                // Ignore cleanup errors
                            }
                        }
                        val updated = uiState.formState.step7.photoUrls.filter { it != photoToDelete }.toMutableList()
                        viewModel.updateStep7(PhotosForm(updated))
                        photoToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete_button))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { photoToDelete = null }
                ) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSummaryReviewScreen(navController: NavController, biodataId: String) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val database = BioDataDatabase.getInstance(context)
    val viewModel = remember {
        com.biodataai.app.ui.viewmodel.AiSummaryViewModel(context, biodataId, firebaseAuth, database)
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
            TopAppBar(title = { Text("Biodata Summary") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.height(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Generating your biodata summary...")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.quotaExceeded) {
                    item {
                        val activity = context as? android.app.Activity
                        // Pre-load the rewarded ad as soon as the cap is hit so the button is ready.
                        LaunchedEffect(Unit) { AdManager.loadRewardedAd(context) }
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    stringResource(R.string.ai_daily_limit_title),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(stringResource(R.string.ai_daily_limit_body), fontSize = 14.sp)
                                if (uiState.adRewardAvailable) {
                                    Button(
                                        onClick = {
                                            val uid = firebaseAuth.currentUser?.uid
                                            if (activity != null && uid != null) {
                                                val shown = AdManager.showRewardedAd(
                                                    activity = activity,
                                                    firebaseUid = uid,
                                                    onAdDismissed = { viewModel.retryAfterAdReward() }
                                                )
                                                // Not loaded yet — fetch one for the next tap.
                                                if (!shown) AdManager.loadRewardedAd(context)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(stringResource(R.string.ai_watch_ad_for_more))
                                    }
                                }
                                OutlinedButton(
                                    onClick = { viewModel.skipAiAndWriteManually() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.ai_write_own_instead))
                                }
                            }
                        }
                    }
                }

                item {
                    if (uiState.isManualEntry) {
                        Text("Write Your Summary", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    } else {
                        Text("AI-Generated Summary", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                item {
                    OutlinedTextField(
                        value = uiState.summary,
                        onValueChange = { viewModel.updateSummary(it) },
                        label = { Text("Summary") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        maxLines = 8
                    )
                }

                if (!uiState.isManualEntry && uiState.summary.isNotEmpty()) {
                    item {
                        OutlinedButton(
                            onClick = { viewModel.skipAiAndWriteManually() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Write Your Own Summary Instead")
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.back))
                        }

                        Button(
                            onClick = {
                                navController.navigate(NavRoute.TemplatePicker(biodataId)) {
                                    popUpTo(NavRoute.AiSummaryReview(biodataId)) { inclusive = true }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.summary.isNotEmpty()
                        ) {
                            Text("Next")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatePickerScreen(navController: NavController, biodataId: String) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val database = BioDataDatabase.getInstance(context)
    val viewModel = remember {
        com.biodataai.app.ui.viewmodel.TemplatePickerViewModel(context, biodataId, firebaseAuth, database)
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
            TopAppBar(title = { Text(stringResource(R.string.choose_template)) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(stringResource(R.string.select_template), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                items(uiState.templates.size) { index ->
                    val template = uiState.templates[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectTemplate(template.id) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                template.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(template.description, fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            if (uiState.selectedTemplateId == template.id) {
                                Text("✓ Selected", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.back))
                        }

                        Button(
                            onClick = {
                                navController.navigate(
                                    NavRoute.BiodataPreview(biodataId, uiState.selectedTemplateId ?: "classic")
                                ) {
                                    popUpTo(NavRoute.TemplatePicker(biodataId)) { inclusive = true }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.selectedTemplateId != null
                        ) {
                            Text(stringResource(R.string.preview))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiodataPreviewScreen(navController: NavController, biodataId: String, templateId: String = "classic", summary: String = "") {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val database = BioDataDatabase.getInstance(context)
    val viewModel = remember {
        com.biodataai.app.ui.viewmodel.BiodataPreviewViewModel(context, biodataId, templateId, summary, firebaseAuth, database)
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
            TopAppBar(title = { Text(uiState.title) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        com.biodataai.app.ui.component.BiodataDocumentView(blocks = uiState.blocks)
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.back))
                        }

                        Button(
                            onClick = {
                                navController.navigate(NavRoute.PdfExport(biodataId, templateId)) {
                                    popUpTo(NavRoute.BiodataPreview(biodataId, templateId)) { inclusive = true }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.export_pdf))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfExportScreen(navController: NavController, biodataId: String, templateId: String = "classic") {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val database = BioDataDatabase.getInstance(context)
    val viewModel = remember {
        com.biodataai.app.ui.viewmodel.PdfExportViewModel(context, biodataId, templateId, firebaseAuth, database)
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isExportSuccessful) {
        if (uiState.isExportSuccessful) {
            snackbarHostState.showSnackbar("PDF exported successfully!")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Export Biodata") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.height(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(uiState.exportProgress)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Export to PDF", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Save your biodata as a PDF document", fontSize = 12.sp)
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Template: ${templateId.replaceFirstChar { it.uppercase() }}", fontWeight = FontWeight.Bold)
                            Text("Format: A4 Portrait", fontSize = 12.sp)
                            Text("File location: Documents folder", fontSize = 12.sp)
                        }
                    }
                }

                if (uiState.isExportSuccessful && uiState.pdfFilePath != null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("✓ PDF Exported", fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                                Text(uiState.pdfFilePath!!, fontSize = 11.sp)
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.back))
                        }

                        Button(
                            onClick = { viewModel.exportPdf() },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isLoading && !uiState.isExportSuccessful
                        ) {
                            Text(stringResource(R.string.export_pdf))
                        }
                    }
                }

                item {
                    if (uiState.isExportSuccessful) {
                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}

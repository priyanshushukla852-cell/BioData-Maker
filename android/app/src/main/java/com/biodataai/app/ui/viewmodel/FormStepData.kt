package com.biodataai.app.ui.viewmodel

/**
 * Step 1: Personal Details
 */
data class PersonalDetailsForm(
    val fullName: String = "",
    val dob: String = "", // YYYY-MM-DD
    val gender: String = "", // MALE, FEMALE, OTHER
    val religion: String = "",
    val caste: String = "",
    val gotra: String = "",
    val heightCm: String = "",
    val complexion: String = "",
    val maritalStatus: String = "", // NEVER_MARRIED, DIVORCED, WIDOWED
    val disability: String = "",
    val bloodGroup: String = "" // e.g. O+, A-, AB+
)

/**
 * Step 2: Family Details
 */
data class FamilyDetailsForm(
    val fatherName: String = "",
    val fatherOccupation: String = "",
    val motherName: String = "",
    val motherOccupation: String = "",
    val siblingsCount: String = "",
    val siblingDetails: String = "",
    val familyStatus: String = "", // WELL_TO_DO, MIDDLE_CLASS, POOR
    val familyType: String = "", // JOINT, NUCLEAR
    val familyValues: String = "" // e.g. Traditional, Moderate, Liberal
)

/**
 * Step 3: Education & Career
 */
data class EducationCareerForm(
    val educationLevel: String = "", // 10TH, 12TH, BACHELOR, MASTER, PHD
    val educationField: String = "",
    val occupation: String = "",
    val companyName: String = "",
    val designation: String = "",
    val income: String = "", // Annual income (optional per CLAUDE.md)
    val currency: String = "INR",
    val college: String = "",
    val workLocation: String = ""
)

/**
 * Step 4: Lifestyle
 */
data class LifestyleForm(
    val diet: String = "", // VEG, NON_VEG
    val smoking: String = "", // YES, NO, OCCASIONALLY
    val drinking: String = "", // YES, NO, OCCASIONALLY
    val hobbies: String = "",
    val languages: String = "", // Comma-separated
    val interests: String = ""
)

/**
 * Step 5: Astrology (Optional)
 */
data class AstrologyForm(
    val birthTime: String = "", // HH:MM (optional)
    val birthPlace: String = "",
    val sunSign: String = "",
    val moonSign: String = "",
    val isManglik: String = "", // YES, NO, UNKNOWN (optional per CLAUDE.md)
    val nakshatra: String = ""
)

/**
 * Step 6: Contact Info
 */
data class ContactInfoForm(
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val postalCode: String = "",
    val country: String = "India"
)

/**
 * Step 7: Photos (handled separately via PhotoUploadScreen)
 */
data class PhotosForm(
    val photoUrls: List<String> = emptyList()
)

/**
 * Combined form state for all steps
 */
data class FormState(
    val step1: PersonalDetailsForm = PersonalDetailsForm(),
    val step2: FamilyDetailsForm = FamilyDetailsForm(),
    val step3: EducationCareerForm = EducationCareerForm(),
    val step4: LifestyleForm = LifestyleForm(),
    val step5: AstrologyForm = AstrologyForm(),
    val step6: ContactInfoForm = ContactInfoForm(),
    val step7: PhotosForm = PhotosForm()
)

package com.biodataai.app.network.api

import com.biodataai.app.ui.viewmodel.FormState

/**
 * Structured section DTOs mirroring the backend's /api/biodatas update contract. The backend stores
 * each biodata section separately (per the ERD) and builds the AI summary prompt from these typed
 * fields — crucially, it redacts income/caste/Manglik before calling Gemini (CLAUDE.md 7.1), which
 * only works if data arrives structured rather than as an opaque blob. So the app maps its
 * [FormState] into these sections on sync rather than sending raw formDataJson.
 *
 * Field names match the backend record components exactly (Jackson binds by name). Enum-typed
 * backend fields receive the enum constant name as a string (e.g. diet "NONVEG"); blanks become
 * null so the backend leaves those columns untouched.
 */
data class PersonalDetailsDto(
    val fullName: String?,
    val dob: String?,        // ISO yyyy-MM-dd
    val gender: String?,     // MALE | FEMALE | OTHER
    val religion: String?,
    val caste: String?,
    val gotra: String?,
    val heightCm: Int?,
    val complexion: String?,
    val disability: String?,
    val maritalStatus: String?,
    val bloodGroup: String?
)

data class FamilyDetailsDto(
    val fatherName: String?,
    val fatherOccupation: String?,
    val motherName: String?,
    val motherOccupation: String?,
    val siblings: String?,
    val familyType: String?,
    val familyValues: String?,
    val familyStatus: String?
)

data class EducationCareerDto(
    val highestQualification: String?,
    val college: String?,
    val jobTitle: String?,
    val company: String?,
    val annualIncome: String?,
    val workLocation: String?,
    val educationField: String?
)

data class LifestyleDto(
    val diet: String?,       // VEG | NONVEG
    val drinking: String?,   // NO | YES | OCCASIONALLY
    val smoking: String?,    // NO | YES | OCCASIONALLY
    val hobbies: String?,
    val languagesSpoken: String?,
    val interests: String?
)

data class AstrologyDto(
    val rashi: String?,
    val nakshatra: String?,
    val manglik: String?,    // YES | NO | PARTIAL
    val birthTime: String?,  // ISO HH:mm
    val birthPlace: String?,
    val sunSign: String?
)

data class ContactInfoDto(
    val contactPhone: String?,
    val contactEmail: String?,
    val city: String?,
    val state: String?,
    val country: String?,
    val address: String?,
    val postalCode: String?
)

private fun String.orNull(): String? = trim().ifBlank { null }

// The backend parses birthTime as a LocalTime; sending a malformed value would fail Jackson
// binding for the whole request, silently blocking the entire sync. Only forward valid HH:mm.
private val HHMM = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")
private fun String.asHhmmOrNull(): String? = orNull()?.takeIf { HHMM.matches(it) }

/**
 * Maps the local [FormState] into the backend's structured update request. Every form field now
 * has a dedicated backend column (added in migration V5), so nothing is dropped. The only
 * semantic mapping that remains is astrology.moonSign -> rashi (rashi *is* the moon sign in Vedic
 * astrology; sunSign has its own column).
 */
fun FormState.toUpdateRequest(title: String? = null): UpdateBiodataRequest {
    val p = step1
    val f = step2
    val e = step3
    val l = step4
    val a = step5
    val c = step6

    val personal = PersonalDetailsDto(
        fullName = p.fullName.orNull(),
        dob = p.dob.orNull(),
        gender = p.gender.orNull(),
        religion = p.religion.orNull(),
        caste = p.caste.orNull(),
        gotra = p.gotra.orNull(),
        heightCm = p.heightCm.orNull()?.toIntOrNull(),
        complexion = p.complexion.orNull(),
        disability = p.disability.orNull(),
        maritalStatus = p.maritalStatus.orNull(),
        bloodGroup = p.bloodGroup.orNull()
    )

    val family = FamilyDetailsDto(
        fatherName = f.fatherName.orNull(),
        fatherOccupation = f.fatherOccupation.orNull(),
        motherName = f.motherName.orNull(),
        motherOccupation = f.motherOccupation.orNull(),
        siblings = f.siblingDetails.orNull() ?: f.siblingsCount.orNull(),
        familyType = f.familyType.orNull(),
        familyValues = f.familyValues.orNull(),
        familyStatus = f.familyStatus.orNull()
    )

    val annualIncome = e.income.orNull()?.let { inc ->
        e.currency.orNull()?.let { "$inc $it" } ?: inc
    }
    val education = EducationCareerDto(
        highestQualification = e.educationLevel.orNull(),
        college = e.college.orNull(),
        jobTitle = e.designation.orNull() ?: e.occupation.orNull(),
        company = e.companyName.orNull(),
        annualIncome = annualIncome,
        workLocation = e.workLocation.orNull(),
        educationField = e.educationField.orNull()
    )

    val lifestyle = LifestyleDto(
        diet = when (l.diet.trim().uppercase()) {
            "VEG" -> "VEG"
            "NON_VEG", "NONVEG" -> "NONVEG"
            else -> null
        },
        drinking = mapHabitFrequency(l.drinking),
        smoking = mapHabitFrequency(l.smoking),
        hobbies = l.hobbies.orNull(),
        languagesSpoken = l.languages.orNull(),
        interests = l.interests.orNull()
    )

    val astrology = AstrologyDto(
        rashi = a.moonSign.orNull(),
        nakshatra = a.nakshatra.orNull(),
        manglik = when (a.isManglik.trim().uppercase()) {
            "YES" -> "YES"
            "NO" -> "NO"
            "PARTIAL" -> "PARTIAL"
            else -> null // UNKNOWN / blank -> leave unset (backend has no UNKNOWN)
        },
        birthTime = a.birthTime.asHhmmOrNull(),
        birthPlace = a.birthPlace.orNull(),
        sunSign = a.sunSign.orNull()
    )

    val contact = ContactInfoDto(
        contactPhone = c.phone.orNull(),
        contactEmail = c.email.orNull(),
        city = c.city.orNull(),
        state = c.state.orNull(),
        country = c.country.orNull(),
        address = c.address.orNull(),
        postalCode = c.postalCode.orNull()
    )

    return UpdateBiodataRequest(
        title = title,
        personalDetails = personal.takeIfAny(),
        familyDetails = family.takeIfAny(),
        educationCareer = education.takeIfAny(),
        lifestyle = lifestyle.takeIfAny(),
        astrology = astrology.takeIfAny(),
        contactInfo = contact.takeIfAny()
    )
}

private fun mapHabitFrequency(value: String): String? = when (value.trim().uppercase()) {
    "YES" -> "YES"
    "NO" -> "NO"
    "OCCASIONALLY" -> "OCCASIONALLY"
    else -> null
}

// Each section is sent only if at least one field is non-null, so we don't create empty rows.
private fun PersonalDetailsDto.takeIfAny() = takeIf {
    listOfNotNull(fullName, dob, gender, religion, caste, gotra, heightCm, complexion, disability, maritalStatus, bloodGroup).isNotEmpty()
}
private fun FamilyDetailsDto.takeIfAny() = takeIf {
    listOfNotNull(fatherName, fatherOccupation, motherName, motherOccupation, siblings, familyType, familyValues, familyStatus).isNotEmpty()
}
private fun EducationCareerDto.takeIfAny() = takeIf {
    listOfNotNull(highestQualification, college, jobTitle, company, annualIncome, workLocation, educationField).isNotEmpty()
}
private fun LifestyleDto.takeIfAny() = takeIf {
    listOfNotNull(diet, drinking, smoking, hobbies, languagesSpoken, interests).isNotEmpty()
}
private fun AstrologyDto.takeIfAny() = takeIf {
    listOfNotNull(rashi, nakshatra, manglik, birthTime, birthPlace, sunSign).isNotEmpty()
}
private fun ContactInfoDto.takeIfAny() = takeIf {
    listOfNotNull(contactPhone, contactEmail, city, state, country, address, postalCode).isNotEmpty()
}

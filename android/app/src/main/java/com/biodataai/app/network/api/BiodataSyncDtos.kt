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
    val disability: String?
)

data class FamilyDetailsDto(
    val fatherName: String?,
    val fatherOccupation: String?,
    val motherName: String?,
    val motherOccupation: String?,
    val siblings: String?,
    val familyType: String?,
    val familyValues: String?
)

data class EducationCareerDto(
    val highestQualification: String?,
    val college: String?,
    val jobTitle: String?,
    val company: String?,
    val annualIncome: String?,
    val workLocation: String?
)

data class LifestyleDto(
    val diet: String?,       // VEG | NONVEG
    val drinking: String?,   // NO | YES | OCCASIONALLY
    val smoking: String?,    // NO | YES | OCCASIONALLY
    val hobbies: String?,
    val languagesSpoken: String?
)

data class AstrologyDto(
    val rashi: String?,
    val nakshatra: String?,
    val manglik: String?,    // YES | NO | PARTIAL
    val birthTime: String?,  // ISO HH:mm
    val birthPlace: String?
)

data class ContactInfoDto(
    val contactPhone: String?,
    val contactEmail: String?,
    val city: String?,
    val state: String?,
    val country: String?
)

private fun String.orNull(): String? = trim().ifBlank { null }

/**
 * Maps the local [FormState] into the backend's structured update request.
 *
 * Notes on field gaps between the app's form and the backend ERD (data that is intentionally
 * remapped or dropped on sync — surfaced so it isn't a silent surprise):
 *  - personal.maritalStatus, education.educationField, contact.address/postalCode, astrology.sunSign
 *    have no backend column and are not sent.
 *  - family.familyStatus (wealth) is placed into the backend's free-text familyValues.
 *  - lifestyle.interests is appended to hobbies (backend has no separate interests column).
 *  - astrology.moonSign maps to rashi.
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
        disability = null
    )

    val family = FamilyDetailsDto(
        fatherName = f.fatherName.orNull(),
        fatherOccupation = f.fatherOccupation.orNull(),
        motherName = f.motherName.orNull(),
        motherOccupation = f.motherOccupation.orNull(),
        siblings = f.siblingDetails.orNull() ?: f.siblingsCount.orNull(),
        familyType = f.familyType.orNull(),
        familyValues = f.familyStatus.orNull()
    )

    val annualIncome = e.income.orNull()?.let { inc ->
        e.currency.orNull()?.let { "$inc $it" } ?: inc
    }
    val education = EducationCareerDto(
        highestQualification = e.educationLevel.orNull(),
        college = null,
        jobTitle = e.designation.orNull() ?: e.occupation.orNull(),
        company = e.companyName.orNull(),
        annualIncome = annualIncome,
        workLocation = null
    )

    val hobbies = listOfNotNull(l.hobbies.orNull(), l.interests.orNull())
        .joinToString(", ").orNull()
    val lifestyle = LifestyleDto(
        diet = when (l.diet.trim().uppercase()) {
            "VEG" -> "VEG"
            "NON_VEG", "NONVEG" -> "NONVEG"
            else -> null
        },
        drinking = mapHabitFrequency(l.drinking),
        smoking = mapHabitFrequency(l.smoking),
        hobbies = hobbies,
        languagesSpoken = l.languages.orNull()
    )

    val astrology = AstrologyDto(
        rashi = a.moonSign.orNull(),
        nakshatra = null,
        manglik = when (a.isManglik.trim().uppercase()) {
            "YES" -> "YES"
            "NO" -> "NO"
            else -> null // UNKNOWN / blank -> leave unset (backend has no UNKNOWN)
        },
        birthTime = a.birthTime.orNull(),
        birthPlace = a.birthPlace.orNull()
    )

    val contact = ContactInfoDto(
        contactPhone = c.phone.orNull(),
        contactEmail = c.email.orNull(),
        city = c.city.orNull(),
        state = c.state.orNull(),
        country = c.country.orNull()
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
    listOfNotNull(fullName, dob, gender, religion, caste, gotra, heightCm, complexion, disability).isNotEmpty()
}
private fun FamilyDetailsDto.takeIfAny() = takeIf {
    listOfNotNull(fatherName, fatherOccupation, motherName, motherOccupation, siblings, familyType, familyValues).isNotEmpty()
}
private fun EducationCareerDto.takeIfAny() = takeIf {
    listOfNotNull(highestQualification, college, jobTitle, company, annualIncome, workLocation).isNotEmpty()
}
private fun LifestyleDto.takeIfAny() = takeIf {
    listOfNotNull(diet, drinking, smoking, hobbies, languagesSpoken).isNotEmpty()
}
private fun AstrologyDto.takeIfAny() = takeIf {
    listOfNotNull(rashi, nakshatra, manglik, birthTime, birthPlace).isNotEmpty()
}
private fun ContactInfoDto.takeIfAny() = takeIf {
    listOfNotNull(contactPhone, contactEmail, city, state, country).isNotEmpty()
}

package com.biodataai.app.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.Instant

enum class BiodataStatus {
    DRAFT, DONE
}

enum class LanguagePref {
    EN, HI
}

@Entity(
    tableName = "biodatas",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["firebaseUid"],
            childColumns = ["userFirebaseUid"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BiodataEntity(
    @PrimaryKey
    val id: String,
    val userFirebaseUid: String,
    val title: String,
    val language: LanguagePref = LanguagePref.EN,
    val status: BiodataStatus = BiodataStatus.DRAFT,
    val templateId: String? = null,
    val formDataJson: String? = null, // JSON serialization of FormState (offline draft cache)
    val syncedAt: Instant? = null, // Last successful backend sync
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant? = null
)

@Entity(
    tableName = "personal_details",
    foreignKeys = [
        ForeignKey(
            entity = BiodataEntity::class,
            parentColumns = ["id"],
            childColumns = ["biodataId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PersonalDetailsEntity(
    @PrimaryKey
    val biodataId: String,
    val fullName: String? = null,
    val dob: String? = null,
    val gender: String? = null,
    val religion: String? = null,
    val caste: String? = null,
    val complexion: String? = null,
    val heightCm: Int? = null,
    val updatedAt: Instant
)

@Entity(
    tableName = "family_details",
    foreignKeys = [
        ForeignKey(
            entity = BiodataEntity::class,
            parentColumns = ["id"],
            childColumns = ["biodataId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FamilyDetailsEntity(
    @PrimaryKey
    val biodataId: String,
    val fatherName: String? = null,
    val motherName: String? = null,
    val fatherOccupation: String? = null,
    val motherOccupation: String? = null,
    val siblings: Int? = null,
    val familyBackground: String? = null,
    val familyValues: String? = null,
    val updatedAt: Instant
)

@Entity(
    tableName = "education_career",
    foreignKeys = [
        ForeignKey(
            entity = BiodataEntity::class,
            parentColumns = ["id"],
            childColumns = ["biodataId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EducationCareerEntity(
    @PrimaryKey
    val biodataId: String,
    val highestQualification: String? = null,
    val college: String? = null,
    val field: String? = null,
    val jobTitle: String? = null,
    val company: String? = null,
    val workLocation: String? = null,
    val income: Long? = null,
    val updatedAt: Instant
)

@Entity(
    tableName = "lifestyle",
    foreignKeys = [
        ForeignKey(
            entity = BiodataEntity::class,
            parentColumns = ["id"],
            childColumns = ["biodataId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LifestyleEntity(
    @PrimaryKey
    val biodataId: String,
    val diet: String? = null,
    val smokingFrequency: String? = null,
    val alcoholFrequency: String? = null,
    val hobbies: String? = null,
    val interests: String? = null,
    val updatedAt: Instant
)

@Entity(
    tableName = "astrology",
    foreignKeys = [
        ForeignKey(
            entity = BiodataEntity::class,
            parentColumns = ["id"],
            childColumns = ["biodataId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AstrologyEntity(
    @PrimaryKey
    val biodataId: String,
    val nakshatra: String? = null,
    val manglik: String? = null,
    val updatedAt: Instant
)

@Entity(
    tableName = "contact_info",
    foreignKeys = [
        ForeignKey(
            entity = BiodataEntity::class,
            parentColumns = ["id"],
            childColumns = ["biodataId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ContactInfoEntity(
    @PrimaryKey
    val biodataId: String,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val updatedAt: Instant
)

@Entity(
    tableName = "biodata_photos",
    foreignKeys = [
        ForeignKey(
            entity = BiodataEntity::class,
            parentColumns = ["id"],
            childColumns = ["biodataId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BiodataPhotoEntity(
    @PrimaryKey
    val id: String,
    val biodataId: String,
    val localUri: String? = null,
    val remoteUrl: String? = null,
    val sortOrder: Int,
    val uploadedAt: Instant? = null
)

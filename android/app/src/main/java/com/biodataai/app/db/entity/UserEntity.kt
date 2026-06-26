package com.biodataai.app.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val firebaseUid: String,
    val displayName: String?,
    val phoneNumber: String?,
    val email: String?,
    val profilePhotoUrl: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

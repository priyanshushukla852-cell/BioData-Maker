package com.biodataai.app.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.biodataai.app.db.entity.BiodataEntity
import com.biodataai.app.db.entity.PersonalDetailsEntity
import com.biodataai.app.db.entity.FamilyDetailsEntity
import com.biodataai.app.db.entity.EducationCareerEntity
import com.biodataai.app.db.entity.LifestyleEntity
import com.biodataai.app.db.entity.AstrologyEntity
import com.biodataai.app.db.entity.ContactInfoEntity
import com.biodataai.app.db.entity.BiodataPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BiodataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBiodata(biodata: BiodataEntity)

    @Update
    suspend fun updateBiodata(biodata: BiodataEntity)

    @Query("SELECT * FROM biodatas WHERE id = :id AND deletedAt IS NULL")
    suspend fun getBiodataById(id: String): BiodataEntity?

    @Query("SELECT * FROM biodatas WHERE userFirebaseUid = :userFirebaseUid AND deletedAt IS NULL ORDER BY updatedAt DESC")
    fun getBiodatasByUser(userFirebaseUid: String): Flow<List<BiodataEntity>>

    @Query("UPDATE biodatas SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteBiodata(id: String, deletedAt: java.time.Instant)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonalDetails(details: PersonalDetailsEntity)

    @Query("SELECT * FROM personal_details WHERE biodataId = :biodataId")
    suspend fun getPersonalDetails(biodataId: String): PersonalDetailsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyDetails(details: FamilyDetailsEntity)

    @Query("SELECT * FROM family_details WHERE biodataId = :biodataId")
    suspend fun getFamilyDetails(biodataId: String): FamilyDetailsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEducationCareer(details: EducationCareerEntity)

    @Query("SELECT * FROM education_career WHERE biodataId = :biodataId")
    suspend fun getEducationCareer(biodataId: String): EducationCareerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLifestyle(details: LifestyleEntity)

    @Query("SELECT * FROM lifestyle WHERE biodataId = :biodataId")
    suspend fun getLifestyle(biodataId: String): LifestyleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAstrology(details: AstrologyEntity)

    @Query("SELECT * FROM astrology WHERE biodataId = :biodataId")
    suspend fun getAstrology(biodataId: String): AstrologyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactInfo(details: ContactInfoEntity)

    @Query("SELECT * FROM contact_info WHERE biodataId = :biodataId")
    suspend fun getContactInfo(biodataId: String): ContactInfoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: BiodataPhotoEntity)

    @Query("SELECT * FROM biodata_photos WHERE biodataId = :biodataId ORDER BY sortOrder ASC")
    suspend fun getPhotosByBiodata(biodataId: String): List<BiodataPhotoEntity>

    @Query("DELETE FROM biodata_photos WHERE id = :photoId")
    suspend fun deletePhoto(photoId: String)
}

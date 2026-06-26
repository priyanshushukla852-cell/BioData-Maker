package com.biodataai.app.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.biodataai.app.db.entity.UserEntity
import com.biodataai.app.db.entity.BiodataEntity
import com.biodataai.app.db.entity.PersonalDetailsEntity
import com.biodataai.app.db.entity.FamilyDetailsEntity
import com.biodataai.app.db.entity.EducationCareerEntity
import com.biodataai.app.db.entity.LifestyleEntity
import com.biodataai.app.db.entity.AstrologyEntity
import com.biodataai.app.db.entity.ContactInfoEntity
import com.biodataai.app.db.entity.BiodataPhotoEntity
import com.biodataai.app.db.dao.BiodataDao
import com.biodataai.app.db.converter.InstantConverter

@Database(
    entities = [
        UserEntity::class,
        BiodataEntity::class,
        PersonalDetailsEntity::class,
        FamilyDetailsEntity::class,
        EducationCareerEntity::class,
        LifestyleEntity::class,
        AstrologyEntity::class,
        ContactInfoEntity::class,
        BiodataPhotoEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(InstantConverter::class)
abstract class BioDataDatabase : RoomDatabase() {
    abstract fun biodataDao(): BiodataDao
}

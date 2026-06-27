package com.biodataai.app.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = false
)
@TypeConverters(InstantConverter::class)
abstract class BioDataDatabase : RoomDatabase() {
    abstract fun biodataDao(): BiodataDao

    companion object {
        @Volatile
        private var instance: BioDataDatabase? = null

        /**
         * v1 -> v2: add indices on the foreign-key columns that aren't already covered
         * by a primary key, to avoid full-table scans when the parent row is modified.
         * Index names match Room's generated convention (index_<table>_<column>) so the
         * runtime schema validation passes.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_biodatas_userFirebaseUid` " +
                        "ON `biodatas` (`userFirebaseUid`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_biodata_photos_biodataId` " +
                        "ON `biodata_photos` (`biodataId`)"
                )
            }
        }

        fun getInstance(context: Context): BioDataDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BioDataDatabase::class.java,
                    "biodata.db"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
        }
    }
}

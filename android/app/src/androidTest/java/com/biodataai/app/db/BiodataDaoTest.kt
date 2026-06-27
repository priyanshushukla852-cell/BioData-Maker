package com.biodataai.app.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.biodataai.app.db.dao.BiodataDao
import com.biodataai.app.db.entity.BiodataEntity
import com.biodataai.app.db.entity.BiodataStatus
import com.biodataai.app.db.entity.PersonalDetailsEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * Instrumented tests for [BiodataDao] — verifies the offline-first persistence
 * premise of the app (CLAUDE.md rule 3: drafts must work fully offline via Room).
 *
 * Uses an in-memory database so each test starts clean. The users table is seeded
 * via execSQL because BiodataEntity has a foreign key to it and there is no UserDao.
 */
@RunWith(AndroidJUnit4::class)
class BiodataDaoTest {

    private lateinit var db: BioDataDatabase
    private lateinit var dao: BiodataDao

    private val testUserUid = "test-user-uid"

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, BioDataDatabase::class.java)
            // Allow synchronous queries on the test thread for deterministic assertions.
            .allowMainThreadQueries()
            .build()
        dao = db.biodataDao()
        seedUser(testUserUid)
    }

    @After
    fun tearDown() {
        db.close()
    }

    /** Insert the FK parent row directly — there is no UserDao to do this for us. */
    private fun seedUser(uid: String) {
        val now = Instant.now().toEpochMilli()
        db.openHelper.writableDatabase.execSQL(
            "INSERT INTO users (firebaseUid, displayName, phoneNumber, email, profilePhotoUrl, createdAt, updatedAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any?>(uid, "Test User", null, null, null, now, now)
        )
    }

    private fun makeBiodata(
        id: String,
        title: String = "My Biodata",
        status: BiodataStatus = BiodataStatus.DRAFT,
        syncedAt: Instant? = null,
        updatedAt: Instant = Instant.now()
    ) = BiodataEntity(
        id = id,
        userFirebaseUid = testUserUid,
        title = title,
        status = status,
        formDataJson = """{"step":1}""",
        syncedAt = syncedAt,
        createdAt = Instant.now(),
        updatedAt = updatedAt
    )

    @Test
    fun insertAndGetBiodata_roundTripsDraft() = runBlocking {
        val biodata = makeBiodata("bd-1", title = "Priya's Biodata")
        dao.insertBiodata(biodata)

        val loaded = dao.getBiodataById("bd-1")
        assertNotNull("Draft should persist offline", loaded)
        assertEquals("Priya's Biodata", loaded!!.title)
        assertEquals(BiodataStatus.DRAFT, loaded.status)
        assertEquals("""{"step":1}""", loaded.formDataJson)
    }

    @Test
    fun getBiodataById_returnsNull_whenSoftDeleted() = runBlocking {
        dao.insertBiodata(makeBiodata("bd-2"))
        dao.softDeleteBiodata("bd-2", Instant.now())

        val loaded = dao.getBiodataById("bd-2")
        assertNull("Soft-deleted biodata must not be returned (CLAUDE.md: soft delete only)", loaded)
    }

    @Test
    fun getBiodatasByUser_excludesSoftDeleted_andOrdersByUpdatedDesc() = runBlocking {
        val older = makeBiodata("bd-old", title = "Older", updatedAt = Instant.now().minusSeconds(60))
        val newer = makeBiodata("bd-new", title = "Newer", updatedAt = Instant.now())
        val deleted = makeBiodata("bd-del", title = "Deleted")
        dao.insertBiodata(older)
        dao.insertBiodata(newer)
        dao.insertBiodata(deleted)
        dao.softDeleteBiodata("bd-del", Instant.now())

        val list = dao.getBiodatasByUser(testUserUid).first()
        assertEquals("Soft-deleted row should be excluded", 2, list.size)
        assertEquals("Most recently updated should come first", "bd-new", list[0].id)
        assertEquals("bd-old", list[1].id)
    }

    @Test
    fun updateBiodata_persistsFormDataChanges() = runBlocking {
        val original = makeBiodata("bd-3")
        dao.insertBiodata(original)

        val edited = original.copy(formDataJson = """{"step":7,"done":true}""", updatedAt = Instant.now())
        dao.updateBiodata(edited)

        val loaded = dao.getBiodataById("bd-3")
        assertEquals("""{"step":7,"done":true}""", loaded!!.formDataJson)
    }

    @Test
    fun getUnsyncedBiodatas_returnsNeverSynced_andStaleSynced() = runBlocking {
        // Never synced (syncedAt == null) — must be picked up.
        dao.insertBiodata(makeBiodata("bd-unsynced", syncedAt = null))
        // Synced before last edit (syncedAt < updatedAt) — stale, must be picked up.
        val staleUpdatedAt = Instant.now()
        dao.insertBiodata(
            makeBiodata("bd-stale", syncedAt = staleUpdatedAt.minusSeconds(30), updatedAt = staleUpdatedAt)
        )
        // Fully synced (syncedAt >= updatedAt) — must NOT be picked up.
        val syncedUpdatedAt = Instant.now().minusSeconds(60)
        dao.insertBiodata(
            makeBiodata("bd-synced", syncedAt = Instant.now(), updatedAt = syncedUpdatedAt)
        )

        val unsynced = dao.getUnsyncedBiodatas(testUserUid).map { it.id }.toSet()
        assertTrue("Never-synced draft must queue for sync", unsynced.contains("bd-unsynced"))
        assertTrue("Stale draft (edited after last sync) must re-queue", unsynced.contains("bd-stale"))
        assertTrue("Already-synced draft must not re-queue", !unsynced.contains("bd-synced"))
    }

    @Test
    fun childDetails_cascadeAndRoundTrip() = runBlocking {
        dao.insertBiodata(makeBiodata("bd-4"))
        val details = PersonalDetailsEntity(
            biodataId = "bd-4",
            fullName = "Priyanshu Shukla",
            gender = "Male",
            heightCm = 175,
            updatedAt = Instant.now()
        )
        dao.insertPersonalDetails(details)

        val loaded = dao.getPersonalDetails("bd-4")
        assertNotNull(loaded)
        assertEquals("Priyanshu Shukla", loaded!!.fullName)
        assertEquals(175, loaded.heightCm)
    }
}

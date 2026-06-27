package com.biodataai.app.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.db.entity.BiodataEntity
import com.biodataai.app.db.entity.BiodataStatus
import com.biodataai.app.ui.viewmodel.ContactInfoForm
import com.biodataai.app.ui.viewmodel.FormState
import com.biodataai.app.ui.viewmodel.PersonalDetailsForm
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * Full-screen Compose tests for the template/preview flow. These exercise composables that
 * call FirebaseAuth.getInstance() at composition time — they only run because
 * [com.biodataai.app.FirebaseTestRunner] registers a stub FirebaseApp before the app starts.
 *
 * The preview test seeds the on-disk singleton database (the same instance the screen reads
 * via BioDataDatabase.getInstance) and cleans the seeded rows up afterward so it doesn't
 * pollute other tests or a developer's local DB.
 */
@RunWith(AndroidJUnit4::class)
class BiodataScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val db = BioDataDatabase.getInstance(context)

    private val testUserUid = "screen-test-user"
    private val testBiodataId = "screen-test-biodata"

    @Before
    fun seedDatabase() {
        val now = Instant.now().toEpochMilli()
        // FK parent — there is no UserDao, so insert directly.
        db.openHelper.writableDatabase.execSQL(
            "INSERT OR REPLACE INTO users (firebaseUid, displayName, phoneNumber, email, profilePhotoUrl, createdAt, updatedAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
            arrayOf<Any?>(testUserUid, "Screen Test", null, null, null, now, now)
        )

        val formState = FormState(
            step1 = PersonalDetailsForm(
                fullName = "Priyanshu Shukla",
                dob = "1996-04-12",
                gender = "MALE",
                religion = "Hindu",
                heightCm = "175"
            ),
            step6 = ContactInfoForm(
                phone = "+910000000000",
                email = "test@example.com",
                address = "123 Test Street"
            )
        )

        runBlocking {
            db.biodataDao().insertBiodata(
                BiodataEntity(
                    id = testBiodataId,
                    userFirebaseUid = testUserUid,
                    title = "Screen Test Biodata",
                    status = BiodataStatus.DRAFT,
                    formDataJson = Gson().toJson(formState),
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
            )
        }
    }

    @After
    fun cleanUpDatabase() {
        // Hard-delete the seeded rows from the singleton DB (CASCADE removes the biodata).
        db.openHelper.writableDatabase.execSQL("DELETE FROM users WHERE firebaseUid = ?", arrayOf<Any?>(testUserUid))
    }

    @Test
    fun templatePicker_rendersTitleAndTemplateOptions() {
        composeTestRule.setContent {
            TemplatePickerScreen(
                navController = rememberNavController(),
                biodataId = testBiodataId
            )
        }

        composeTestRule.onNodeWithText("Choose Template").assertIsDisplayed()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Classic").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Modern").assertExists()
        composeTestRule.onNodeWithText("Minimal").assertExists()
    }

    @Test
    fun templatePicker_selectingTemplateShowsSelectedMarker() {
        composeTestRule.setContent {
            TemplatePickerScreen(
                navController = rememberNavController(),
                biodataId = testBiodataId
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Modern").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Modern").performClick()
        composeTestRule.onNodeWithText("✓ Selected", substring = true).assertExists()
    }

    @Test
    fun biodataPreview_rendersSeededBiodataContent() {
        composeTestRule.setContent {
            BiodataPreviewScreen(
                navController = rememberNavController(),
                biodataId = testBiodataId,
                templateId = "classic",
                summary = "A dedicated software engineer."
            )
        }

        // Preview content loads asynchronously from Room via the ViewModel.
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Priyanshu Shukla", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("A dedicated software engineer.", substring = true).assertExists()
    }
}

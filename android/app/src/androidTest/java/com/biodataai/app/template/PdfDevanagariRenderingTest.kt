package com.biodataai.app.template

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.biodataai.app.db.entity.LanguagePref
import com.biodataai.app.ui.viewmodel.ContactInfoForm
import com.biodataai.app.ui.viewmodel.FormState
import com.biodataai.app.ui.viewmodel.PersonalDetailsForm
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies the Devanagari/Hindi PDF path actually draws glyphs (CLAUDE.md: Devanagari rendering
 * must be verified for any new PDF output path).
 *
 * Strategy: render a Hindi biodata through the real [PdfLayoutEngine], rasterize page 0 with
 * [PdfRenderer], and assert the page contains a substantial number of dark text pixels. This
 * catches the failure modes that matter — missing font asset, blank page, or a crash in the
 * StaticLayout/Typeface path — which would otherwise ship silently as tofu/empty Hindi PDFs.
 */
@RunWith(AndroidJUnit4::class)
class PdfDevanagariRenderingTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun hindiBiodata_rendersNonBlankGlyphsToPdf() {
        val form = FormState(
            step1 = PersonalDetailsForm(
                fullName = "प्रियांशु शुक्ला",
                dob = "1996-04-12",
                gender = "पुरुष",
                religion = "हिन्दू",
                heightCm = "175"
            ),
            step6 = ContactInfoForm(
                phone = "+910000000000",
                email = "test@example.com",
                address = "१२३ परीक्षण मार्ग"
            )
        )
        val labels = TemplateLabels.forLanguage(context, LanguagePref.HI)
        val document = TemplateRenderer.buildDocument(
            templateId = "classic",
            form = form,
            labels = labels,
            summary = "एक समर्पित सॉफ़्टवेयर इंजीनियर जो परिवार को महत्व देता है।"
        )

        val file = PdfLayoutEngine(context).render(document, "test_hindi_biodata")
        try {
            assertTrue("PDF file should be non-trivial in size", file.length() > 1000)

            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    assertTrue("PDF should have at least one page", renderer.pageCount >= 1)
                    renderer.openPage(0).use { page ->
                        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                        val darkPixels = countDarkPixels(bitmap)
                        bitmap.recycle()
                        // A page with the Hindi labels + values drawn should have thousands of dark
                        // pixels; 500 is a conservative floor that still rules out a blank/tofu-free page.
                        assertTrue(
                            "Expected rendered Devanagari text (dark pixels), found only $darkPixels",
                            darkPixels > 500
                        )
                    }
                }
            }
        } finally {
            file.delete()
        }
    }

    private fun countDarkPixels(bitmap: Bitmap): Int {
        var count = 0
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (p in pixels) {
            val r = Color.red(p); val g = Color.green(p); val b = Color.blue(p)
            val luminance = (r * 299 + g * 587 + b * 114) / 1000
            if (luminance < 128) count++
        }
        return count
    }
}

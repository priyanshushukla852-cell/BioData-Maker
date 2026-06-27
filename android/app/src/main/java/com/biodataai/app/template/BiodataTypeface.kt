package com.biodataai.app.template

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * Single source for the Devanagari-capable font used by BOTH the PDF engine and the Compose
 * preview. Noto Sans Devanagari covers Latin glyphs too, so one typeface renders mixed
 * English/Hindi lines correctly. Without an embedded Devanagari font, PdfDocument renders Hindi
 * as tofu boxes — this is the file that prevents that (CLAUDE.md: verify Devanagari for any PDF path).
 *
 * The .ttf is bundled at assets/fonts/ (see assets/fonts/NotoSansDevanagari-Regular.ttf).
 */
object BiodataTypeface {

    const val ASSET_PATH = "fonts/NotoSansDevanagari-Regular.ttf"

    @Volatile
    private var cached: Typeface? = null

    /** Regular weight [Typeface] for the PDF Canvas/StaticLayout path. Cached after first load. */
    fun regular(context: Context): Typeface {
        return cached ?: synchronized(this) {
            cached ?: Typeface.createFromAsset(context.assets, ASSET_PATH).also { cached = it }
        }
    }

    /** Bold weight, synthesized from the regular face (only Regular is bundled). */
    fun bold(context: Context): Typeface = Typeface.create(regular(context), Typeface.BOLD)

    /** Compose [FontFamily] backed by the same asset, for the on-screen preview. */
    fun composeFamily(context: Context): FontFamily = FontFamily(
        Font(ASSET_PATH, context.assets, weight = FontWeight.Normal),
        Font(ASSET_PATH, context.assets, weight = FontWeight.Bold)
    )
}

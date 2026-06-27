package com.biodataai.app.template

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.io.File
import java.io.FileOutputStream

/**
 * Renders a [BiodataDocument] to a multi-page A4 PDF using Android's [PdfDocument] + [Canvas]
 * (no iText, fully on-device, works offline).
 *
 * The layout helper handles the three things hand-drawn PDFs always need:
 *  - **Devanagari shaping**: all text is drawn via [StaticLayout] with the bundled Devanagari
 *    [Typeface]. (canvas.drawText does NOT shape Indic conjuncts/matras correctly — StaticLayout
 *    does.)
 *  - **Measurement + line wrapping**: StaticLayout wraps each block to the content width and
 *    reports its measured height.
 *  - **Page-break detection**: a cursor walks down the page; when a block (or the next slice of a
 *    long paragraph) won't fit the remaining height, a new page is started. Paragraphs taller than
 *    a full page are split line-by-line so nothing is clipped.
 */
class PdfLayoutEngine(private val context: Context) {

    // A4 at 72 dpi, in points (same unit PdfDocument uses).
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 48f
    private val contentWidth = (pageWidth - 2 * margin).toInt()
    private val contentBottom = pageHeight - margin

    private val regular: Typeface get() = BiodataTypeface.regular(context)
    private val bold: Typeface get() = BiodataTypeface.bold(context)

    private fun bodyPaint() = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = regular; textSize = 11f; color = Color.BLACK
    }

    private fun labelPaint() = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = bold; textSize = 11f; color = Color.parseColor("#444444")
    }

    private fun sectionPaint() = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = bold; textSize = 13f; color = Color.BLACK
    }

    private fun titlePaint() = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = bold; textSize = 20f; color = Color.BLACK
    }

    private val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#BBBBBB"); strokeWidth = 1f
    }

    /** Renders [document] to a PDF file under the app's external Documents dir. Returns the file. */
    fun render(document: BiodataDocument, fileBaseName: String): File {
        val pdf = PdfDocument()
        val cursor = Cursor(pdf)

        for (block in document.blocks) {
            when (block) {
                is TemplateBlock.Title -> cursor.drawCentered(block.text, titlePaint())
                is TemplateBlock.Section -> {
                    cursor.advance(6f)
                    cursor.drawLayout(staticLayout(block.text, sectionPaint()))
                    cursor.advance(2f)
                }
                is TemplateBlock.Field -> cursor.drawField(block.label, block.value)
                is TemplateBlock.Paragraph -> cursor.drawLayout(staticLayout(block.text, bodyPaint()))
                is TemplateBlock.Divider -> cursor.drawRule()
                is TemplateBlock.Gap -> cursor.advance(block.points)
            }
        }
        cursor.finish()

        val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS)
        if (dir != null && !dir.exists()) dir.mkdirs()
        val file = File(dir, "${fileBaseName}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()
        return file
    }

    private fun staticLayout(text: String, paint: TextPaint, width: Int = contentWidth): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setIncludePad(false)
            .build()

    /** Walks down the document, opening pages as needed. */
    private inner class Cursor(private val pdf: PdfDocument) {
        private var page: PdfDocument.Page = startPage(1)
        private var canvas: Canvas = page.canvas
        private var y: Float = margin

        private fun startPage(number: Int): PdfDocument.Page =
            pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, number).create())

        private fun newPage() {
            pdf.finishPage(page)
            page = startPage(pdf.pages.size + 1)
            canvas = page.canvas
            y = margin
        }

        fun advance(points: Float) {
            y += points
            if (y > contentBottom) newPage()
        }

        fun drawRule() {
            if (y + 8f > contentBottom) newPage()
            y += 4f
            canvas.drawLine(margin, y, pageWidth - margin, y, rulePaint)
            y += 4f
        }

        fun drawCentered(text: String, paint: TextPaint) {
            val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setIncludePad(false)
                .build()
            drawLayout(layout)
        }

        fun drawField(label: String, value: String) {
            // Bold label then the value on the same logical row; value wraps under the row width.
            val labelLayout = staticLayout("$label:", labelPaint())
            val labelWidth = labelLayout.getLineWidth(0) + 6f
            val valueWidth = (contentWidth - labelWidth).toInt().coerceAtLeast(40)
            val valueLayout = staticLayout(value, bodyPaint(), valueWidth)
            val rowHeight = maxOf(labelLayout.height, valueLayout.height).toFloat()

            if (y + rowHeight > contentBottom) newPage()
            // label
            canvas.save(); canvas.translate(margin, y); labelLayout.draw(canvas); canvas.restore()
            // value
            canvas.save(); canvas.translate(margin + labelWidth, y); valueLayout.draw(canvas); canvas.restore()
            y += rowHeight + 4f
        }

        /** Draws a (possibly multi-page) StaticLayout, slicing it across page breaks line-by-line. */
        fun drawLayout(layout: StaticLayout) {
            var lineStart = 0
            while (lineStart < layout.lineCount) {
                if (y >= contentBottom) newPage()
                val available = contentBottom - y
                val sliceTop = layout.getLineTop(lineStart)
                var lineEnd = lineStart
                while (lineEnd < layout.lineCount &&
                    layout.getLineBottom(lineEnd) - sliceTop <= available
                ) lineEnd++

                if (lineEnd == lineStart) { newPage(); continue } // not even one line fits

                val sliceBottom = layout.getLineBottom(lineEnd - 1)
                val sliceHeight = (sliceBottom - sliceTop).toFloat()

                canvas.save()
                canvas.clipRect(margin, y, pageWidth - margin, y + sliceHeight)
                canvas.translate(margin, y - sliceTop)
                layout.draw(canvas)
                canvas.restore()

                y += sliceHeight
                lineStart = lineEnd
                if (lineStart < layout.lineCount) newPage()
            }
            y += 2f
        }

        fun finish() = pdf.finishPage(page)
    }
}

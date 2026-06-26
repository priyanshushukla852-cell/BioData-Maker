package com.biodataai.app.util

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

object PdfExporter {
    data class PdfExportResult(
        val success: Boolean,
        val filePath: String? = null,
        val error: String? = null
    )

    fun exportToPdf(
        context: Context,
        biodataName: String,
        previewContent: String,
        templateId: String
    ): PdfExportResult {
        return try {
            // Create PDF document
            val document = PdfDocument()

            // PDF settings
            val pageWidth = 595 // A4 width in points
            val pageHeight = 842 // A4 height in points
            val margin = 40

            // Create page
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Set up paint for text
            val paint = android.graphics.Paint().apply {
                textSize = 10f
                color = android.graphics.Color.BLACK
            }

            val titlePaint = android.graphics.Paint().apply {
                textSize = 16f
                color = android.graphics.Color.BLACK
                isFakeBoldText = true
            }

            // Draw title
            canvas.drawText("BIODATA - $templateId", margin.toFloat(), (margin + 20).toFloat(), titlePaint)
            canvas.drawText("$biodataName", margin.toFloat(), (margin + 40).toFloat(), titlePaint)

            // Draw divider
            val dividerY = (margin + 60).toFloat()
            canvas.drawLine(margin.toFloat(), dividerY, (pageWidth - margin).toFloat(), dividerY, paint)

            // Draw content with text wrapping
            var currentY = (margin + 80).toFloat()
            val maxWidth = pageWidth - (2 * margin)
            val lineHeight = 12f

            val lines = previewContent.split("\n")
            for (line in lines) {
                if (currentY > pageHeight - margin) {
                    // Start new page if content exceeds page
                    document.finishPage(page)
                    val newPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.pages.size + 1).create()
                    val newPage = document.startPage(newPageInfo)
                    val newCanvas = newPage.canvas
                    currentY = margin.toFloat()
                    newCanvas.drawText(line, margin.toFloat(), currentY, paint)
                } else {
                    canvas.drawText(line, margin.toFloat(), currentY, paint)
                }
                currentY += lineHeight
            }

            document.finishPage(page)

            // Save PDF to documents directory
            val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (documentsDir != null && !documentsDir.exists()) {
                documentsDir.mkdirs()
            }

            val fileName = "Biodata_${biodataName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val filePath = File(documentsDir, fileName)

            val outputStream = FileOutputStream(filePath)
            document.writeTo(outputStream)
            document.close()
            outputStream.close()

            PdfExportResult(
                success = true,
                filePath = filePath.absolutePath
            )
        } catch (e: Exception) {
            PdfExportResult(
                success = false,
                error = "PDF export failed: ${e.javaClass.simpleName}"
            )
        }
    }
}

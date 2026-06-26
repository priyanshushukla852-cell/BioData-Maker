package com.biodataai.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfExporterTest {

    @Test
    fun testPdfExportResultSuccess() {
        val result = PdfExporter.PdfExportResult(
            success = true,
            filePath = "/path/to/biodata.pdf"
        )
        assertTrue(result.success)
        assertEquals("/path/to/biodata.pdf", result.filePath)
        assertNull(result.error)
    }

    @Test
    fun testPdfExportResultFailure() {
        val result = PdfExporter.PdfExportResult(
            success = false,
            error = "File system error"
        )
        assertFalse(result.success)
        assertEquals("File system error", result.error)
        assertNull(result.filePath)
    }

    @Test
    fun testPdfExportResultEmpty() {
        val result = PdfExporter.PdfExportResult(
            success = false,
            error = "Unknown error"
        )
        assertFalse(result.success)
    }
}

package com.biodataai.app.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TemplatePickerViewModelTest {

    @Test
    fun testTemplateOptionCreation() {
        val template = com.biodataai.app.ui.viewmodel.TemplateOption(
            id = "classic",
            name = "Classic",
            description = "Traditional format"
        )
        assertEquals("classic", template.id)
        assertEquals("Classic", template.name)
    }

    @Test
    fun testTemplatePickerUiStateInitial() {
        val state = com.biodataai.app.ui.viewmodel.TemplatePickerUiState()
        assertFalse(state.isLoading)
        assertEquals(0, state.templates.size)
    }

    @Test
    fun testTemplateSelection() {
        val templates = listOf(
            com.biodataai.app.ui.viewmodel.TemplateOption("classic", "Classic", "Traditional"),
            com.biodataai.app.ui.viewmodel.TemplateOption("modern", "Modern", "Contemporary")
        )
        val state = com.biodataai.app.ui.viewmodel.TemplatePickerUiState(
            templates = templates,
            selectedTemplateId = "modern"
        )
        assertEquals("modern", state.selectedTemplateId)
    }

    @Test
    fun testTemplateListLoading() {
        val state = com.biodataai.app.ui.viewmodel.TemplatePickerUiState(
            isLoading = true,
            templates = emptyList()
        )
        assertEquals(true, state.isLoading)
    }
}

package com.biodataai.app.template

import com.biodataai.app.ui.viewmodel.FormState

/**
 * Turns form data + localized [TemplateLabels] into a [BiodataDocument] (an ordered block list).
 * This is the only place a template's *structure* is defined; the layout skeleton is identical
 * across languages — only the label/value text differs — so EN and HI output can't structurally
 * drift (CLAUDE.md: Hindi/English parity).
 *
 * Per CLAUDE.md, income/caste/Manglik are intentionally excluded from the Classic template rather
 * than hardcoded into every output. Empty fields are omitted so the document stays clean.
 *
 * Only the "classic" template is implemented for now; "modern"/"minimal" follow after Hindi
 * rendering is verified.
 */
object TemplateRenderer {

    fun buildDocument(
        templateId: String,
        form: FormState,
        labels: TemplateLabels,
        summary: String
    ): BiodataDocument = when (templateId) {
        else -> classic(form, labels, summary) // only Classic exists yet; others fall back to it
    }

    private fun classic(form: FormState, labels: TemplateLabels, summary: String): BiodataDocument {
        val blocks = buildList {
            add(TemplateBlock.Title(labels.title))
            add(TemplateBlock.Gap(8f))
            add(TemplateBlock.Divider)
            add(TemplateBlock.Gap(8f))

            add(TemplateBlock.Section(labels.sectionPersonal))
            addField(labels.name, form.step1.fullName)
            addField(labels.dob, form.step1.dob)
            addField(labels.gender, form.step1.gender)
            addField(labels.religion, form.step1.religion)
            addField(labels.height, heightWithUnit(form.step1.heightCm, labels.cm))

            add(TemplateBlock.Gap(12f))
            add(TemplateBlock.Section(labels.sectionContact))
            addField(labels.phone, form.step6.phone)
            addField(labels.email, form.step6.email)
            addField(labels.address, contactAddress(form))

            if (summary.isNotBlank()) {
                add(TemplateBlock.Gap(12f))
                add(TemplateBlock.Section(labels.sectionAbout))
                add(TemplateBlock.Paragraph(summary.trim()))
            }
        }
        return BiodataDocument(title = labels.title, blocks = blocks)
    }

    /** Adds a [TemplateBlock.Field] only when the user actually entered a value. */
    private fun MutableList<TemplateBlock>.addField(label: String, value: String) {
        if (value.isNotBlank()) add(TemplateBlock.Field(label, value.trim()))
    }

    private fun heightWithUnit(heightCm: String, cm: String): String =
        if (heightCm.isBlank()) "" else "$heightCm $cm"

    /** Joins the address parts the Classic template shows. */
    private fun contactAddress(form: FormState): String {
        val parts = listOf(form.step6.address, form.step6.city, form.step6.state)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        return parts.joinToString(", ")
    }
}

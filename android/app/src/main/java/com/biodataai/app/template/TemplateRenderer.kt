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
 * Three templates share the engine; they differ only in which sections/fields they show and in
 * what order — never in the layout primitives — so EN/HI parity and the PDF/preview consistency
 * hold across all of them.
 */
object TemplateRenderer {

    fun buildDocument(
        templateId: String,
        form: FormState,
        labels: TemplateLabels,
        summary: String
    ): BiodataDocument = when (templateId) {
        "modern" -> modern(form, labels, summary)
        "minimal" -> minimal(form, labels, summary)
        else -> classic(form, labels, summary)
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

    /**
     * Modern: leads with the person's name as the header, then a professional profile, brief
     * personal facts, and the summary. Education/Career fields come from step3.
     */
    private fun modern(form: FormState, labels: TemplateLabels, summary: String): BiodataDocument {
        val blocks = buildList {
            add(TemplateBlock.Title(form.step1.fullName.ifBlank { labels.title }))
            add(TemplateBlock.Gap(8f))
            add(TemplateBlock.Divider)
            add(TemplateBlock.Gap(8f))

            add(TemplateBlock.Section(labels.sectionProfessional))
            addField(labels.occupation, form.step3.occupation)
            addField(labels.education, form.step3.educationLevel)
            addField(labels.company, form.step3.companyName)

            add(TemplateBlock.Gap(12f))
            add(TemplateBlock.Section(labels.sectionPersonal))
            addField(labels.age, ageFromDob(form.step1.dob))
            addField(labels.height, heightWithUnit(form.step1.heightCm, labels.cm))
            addField(labels.religion, form.step1.religion)

            if (summary.isNotBlank()) {
                add(TemplateBlock.Gap(12f))
                add(TemplateBlock.Section(labels.sectionAbout))
                add(TemplateBlock.Paragraph(summary.trim()))
            }
        }
        return BiodataDocument(title = labels.title, blocks = blocks)
    }

    /**
     * Minimal: name, a single compact identity line, and the summary — no section headers or
     * labelled rows.
     */
    private fun minimal(form: FormState, labels: TemplateLabels, summary: String): BiodataDocument {
        val identity = listOf(form.step1.dob, form.step1.gender)
            .map { it.trim() }.filter { it.isNotEmpty() }.joinToString("  •  ")
        val blocks = buildList {
            add(TemplateBlock.Title(form.step1.fullName.ifBlank { labels.title }))
            if (identity.isNotEmpty()) {
                add(TemplateBlock.Gap(4f))
                add(TemplateBlock.Paragraph(identity))
            }
            if (summary.isNotBlank()) {
                add(TemplateBlock.Gap(12f))
                add(TemplateBlock.Paragraph(summary.trim()))
            }
        }
        return BiodataDocument(title = labels.title, blocks = blocks)
    }

    /** Approximate age in years from a "YYYY-MM-DD" dob; empty if the year can't be parsed. */
    private fun ageFromDob(dob: String): String {
        val birthYear = dob.trim().take(4).toIntOrNull() ?: return ""
        val age = java.time.Year.now().value - birthYear
        return if (age in 1..150) age.toString() else ""
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

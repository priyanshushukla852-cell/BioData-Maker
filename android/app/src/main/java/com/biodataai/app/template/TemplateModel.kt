package com.biodataai.app.template

/**
 * Draw-target-independent description of a rendered biodata. [TemplateRenderer] produces this
 * from form data + localized labels; both the PDF engine ([PdfLayoutEngine]) and the on-screen
 * Compose preview ([com.biodataai.app.ui.component.BiodataDocumentView]) consume the SAME blocks,
 * so the printed PDF and the preview can't drift apart.
 */
data class BiodataDocument(
    val title: String,
    val blocks: List<TemplateBlock>
)

sealed interface TemplateBlock {
    /** Centered document title (e.g. "Marriage Biodata" / "विवाह बायोडाटा"). */
    data class Title(val text: String) : TemplateBlock

    /** Section heading (e.g. "Contact Information"). */
    data class Section(val text: String) : TemplateBlock

    /** A labelled value row ("Name: …"). [value] is user-entered, rendered as-is. */
    data class Field(val label: String, val value: String) : TemplateBlock

    /** Free-flowing multi-line text (e.g. the AI/manual summary). */
    data class Paragraph(val text: String) : TemplateBlock

    /** Horizontal rule. */
    data object Divider : TemplateBlock

    /** Vertical gap, in points (1/72 inch — same unit PdfDocument uses). */
    data class Gap(val points: Float) : TemplateBlock
}

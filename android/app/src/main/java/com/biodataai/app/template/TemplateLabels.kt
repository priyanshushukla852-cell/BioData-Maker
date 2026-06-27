package com.biodataai.app.template

import android.content.Context
import android.content.res.Configuration
import com.biodataai.app.R
import com.biodataai.app.db.entity.LanguagePref
import java.util.Locale

/**
 * Resolves rendered-document labels in the biodata's OWN language, independent of the device
 * locale. A user on an English phone can still export a Hindi biodata and vice-versa, so we read
 * the strings through a locale-overridden resources context rather than plain `getString`.
 *
 * Labels live in strings.xml (values/ + values-hi/) so they stay translatable; only the keys
 * are referenced here.
 */
class TemplateLabels private constructor(
    val title: String,
    val sectionPersonal: String,
    val name: String,
    val dob: String,
    val gender: String,
    val religion: String,
    val height: String,
    val cm: String,
    val sectionContact: String,
    val phone: String,
    val email: String,
    val address: String,
    val sectionAbout: String
) {
    companion object {
        fun forLanguage(context: Context, language: LanguagePref): TemplateLabels {
            val locale = if (language == LanguagePref.HI) Locale.forLanguageTag("hi") else Locale.ENGLISH
            val config = Configuration(context.resources.configuration).apply { setLocale(locale) }
            val res = context.createConfigurationContext(config).resources
            return TemplateLabels(
                title = res.getString(R.string.doc_title),
                sectionPersonal = res.getString(R.string.doc_section_personal),
                name = res.getString(R.string.doc_label_name),
                dob = res.getString(R.string.doc_label_dob),
                gender = res.getString(R.string.doc_label_gender),
                religion = res.getString(R.string.doc_label_religion),
                height = res.getString(R.string.doc_label_height),
                cm = res.getString(R.string.doc_unit_cm),
                sectionContact = res.getString(R.string.doc_section_contact),
                phone = res.getString(R.string.doc_label_phone),
                email = res.getString(R.string.doc_label_email),
                address = res.getString(R.string.doc_label_address),
                sectionAbout = res.getString(R.string.doc_section_about)
            )
        }
    }
}

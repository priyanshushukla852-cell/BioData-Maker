package com.biodataai.app.util

import android.util.Patterns
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Pure input helpers for form fields: DOB display formatting (DD-MM-YYYY with auto-inserted
 * dashes) and email/phone format checks. The DOB field shows DD-MM-YYYY to the user, but storage
 * and the backend stay on ISO yyyy-MM-dd (see [dobDisplayToIso]) so the API contract is unchanged.
 */
object InputValidation {

    private val ISO: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val DISPLAY: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    /**
     * Reformats raw DOB keystrokes into DD-MM-YYYY, auto-inserting '-'. For the day and month
     * segments a leading digit 4-9 can't begin a valid two-digit value, so the dash is inserted
     * immediately; a leading 0-3 waits for the second digit. [allowTrailingDash] is passed false
     * during deletion so a just-completed segment's trailing dash doesn't block backspacing.
     */
    fun formatDobInput(input: String, allowTrailingDash: Boolean): String {
        val digits = input.filter(Char::isDigit).take(8) // DDMMYYYY
        val sb = StringBuilder()
        var i = 0

        fun consumeSegment() {
            val c1 = digits[i]; sb.append(c1); i++
            val complete = if (c1 in '0'..'3') {
                if (i < digits.length) { sb.append(digits[i]); i++; true } else false
            } else {
                true // 4-9: only a single-digit day/month is possible
            }
            if (complete && (i < digits.length || allowTrailingDash)) sb.append('-')
        }

        if (i < digits.length) consumeSegment()                      // day
        if (sb.endsWith("-") && i < digits.length) consumeSegment()  // month
        while (i < digits.length) { sb.append(digits[i]); i++ }      // year
        return sb.toString()
    }

    /**
     * DD-MM-YYYY (single-digit day/month allowed, e.g. "4-5-1990") -> ISO yyyy-MM-dd, or null if it
     * is not yet a complete, real calendar date.
     */
    fun dobDisplayToIso(display: String): String? {
        val parts = display.split("-")
        if (parts.size != 3) return null
        val (d, m, y) = parts
        if (d.isBlank() || m.isBlank() || y.length != 4) return null
        return try {
            LocalDate.of(y.toInt(), m.toInt(), d.toInt()).format(ISO)
        } catch (e: Exception) {
            null
        }
    }

    /** ISO yyyy-MM-dd -> DD-MM-YYYY for display; returns the input unchanged if it isn't ISO. */
    fun isoToDobDisplay(iso: String): String =
        try {
            LocalDate.parse(iso, ISO).format(DISPLAY)
        } catch (e: Exception) {
            iso
        }

    fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    /** Accepts an optional leading '+' and 10-15 digits; spaces and dashes are ignored. */
    fun isValidPhone(phone: String): Boolean {
        val cleaned = phone.trim().replace(" ", "").replace("-", "")
        return Regex("^\\+?[0-9]{10,15}$").matches(cleaned)
    }
}

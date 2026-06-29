package com.biodataai.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InputValidationTest {

    private fun typed(s: String) = InputValidation.formatDobInput(s, allowTrailingDash = true)
    private fun deleted(s: String) = InputValidation.formatDobInput(s, allowTrailingDash = false)

    @Test
    fun day_leading_0to3_waits_for_second_digit() {
        assertEquals("1", typed("1"))
        assertEquals("15-", typed("15"))
        assertEquals("03-", typed("03"))
    }

    @Test
    fun day_leading_4to9_inserts_dash_immediately() {
        assertEquals("5-", typed("5"))
        assertEquals("9-", typed("9"))
    }

    @Test
    fun month_follows_same_rule_as_day() {
        // day 15, then month leading 0 waits, completes at second digit
        assertEquals("15-0", typed("150"))
        assertEquals("15-08-", typed("1508"))
        // day 15, month leading 8 -> single-digit month, dash immediately
        assertEquals("15-8-", typed("158"))
    }

    @Test
    fun full_date_formats_and_stops_at_eight_digits() {
        assertEquals("15-08-1990", typed("15081990"))
        assertEquals("15-08-1990", typed("150819901234")) // extra digits ignored
    }

    @Test
    fun deletion_does_not_re_add_trailing_dash() {
        assertEquals("5", deleted("5"))
        assertEquals("15", deleted("15"))
        assertEquals("15-08-199", deleted("15-08-199"))
    }

    @Test
    fun non_digits_are_stripped() {
        assertEquals("15-08-1990", typed("15/08/1990"))
    }

    @Test
    fun displayToIso_converts_complete_valid_dates() {
        assertEquals("1990-08-15", InputValidation.dobDisplayToIso("15-08-1990"))
        // single-digit day/month are zero-padded
        assertEquals("1990-05-04", InputValidation.dobDisplayToIso("4-5-1990"))
    }

    @Test
    fun displayToIso_rejects_partial_or_invalid_dates() {
        assertNull(InputValidation.dobDisplayToIso("15-08-19"))   // year incomplete
        assertNull(InputValidation.dobDisplayToIso("15-08-"))     // missing year
        assertNull(InputValidation.dobDisplayToIso("31-02-1990")) // not a real date
        assertNull(InputValidation.dobDisplayToIso("15-13-1990")) // month out of range
    }

    @Test
    fun isoToDisplay_roundtrips_and_tolerates_garbage() {
        assertEquals("15-08-1990", InputValidation.isoToDobDisplay("1990-08-15"))
        assertEquals("", InputValidation.isoToDobDisplay("")) // empty stays empty
    }

    @Test
    fun caret_lands_after_the_dash_so_typing_continues_in_next_segment() {
        // Two day digits -> caret jumps past the auto-inserted dash into the month segment.
        assertEquals(3, InputValidation.dobCaretForDigitCount("15-", 2))
        // The bug case: "11-1" then typing "0" -> "11-10"; caret must sit at the end (after 4
        // digits), not be shoved left by the earlier dash.
        assertEquals(5, InputValidation.dobCaretForDigitCount("11-10", 4))
        // Past the second dash, into the year.
        assertEquals(6, InputValidation.dobCaretForDigitCount("11-10-", 4))
        assertEquals(0, InputValidation.dobCaretForDigitCount("15-", 0))
    }
}

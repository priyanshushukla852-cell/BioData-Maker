package com.biodataai.app.ui.viewmodel

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FormStepViewModelTest {

    @Test
    fun testFormStateSerializationToJson() {
        val formState = FormState(
            step1 = PersonalDetailsForm(
                fullName = "John Doe",
                dob = "1990-01-15",
                gender = "MALE",
                religion = "Hindu"
            ),
            step2 = FamilyDetailsForm(
                fatherName = "James Doe",
                motherName = "Mary Doe",
                siblingsCount = "2"
            )
        )

        val json = Gson().toJson(formState)
        assertNotNull(json)
        assert(json.contains("John Doe"))
        assert(json.contains("James Doe"))
        assert(json.contains("MALE"))
    }

    @Test
    fun testFormStateDeserialization() {
        val json = """
            {
                "step1": {"fullName": "Jane Smith", "dob": "1992-05-20", "gender": "FEMALE"},
                "step2": {"fatherName": "Robert Smith"},
                "step3": {},
                "step4": {},
                "step5": {},
                "step6": {},
                "step7": {}
            }
        """.trimIndent()

        val formState = Gson().fromJson(json, FormState::class.java)
        assertEquals("Jane Smith", formState.step1.fullName)
        assertEquals("1992-05-20", formState.step1.dob)
        assertEquals("FEMALE", formState.step1.gender)
        assertEquals("Robert Smith", formState.step2.fatherName)
    }

    @Test
    fun testAllStepsAreSerialized() {
        val formState = FormState(
            step1 = PersonalDetailsForm(fullName = "Test"),
            step2 = FamilyDetailsForm(fatherName = "Father"),
            step3 = EducationCareerForm(occupation = "Engineer"),
            step4 = LifestyleForm(diet = "VEG"),
            step5 = AstrologyForm(birthPlace = "Delhi"),
            step6 = ContactInfoForm(phone = "9876543210"),
            step7 = PhotosForm()
        )

        val json = Gson().toJson(formState)
        val deserialized = Gson().fromJson(json, FormState::class.java)

        assertEquals("Test", deserialized.step1.fullName)
        assertEquals("Father", deserialized.step2.fatherName)
        assertEquals("Engineer", deserialized.step3.occupation)
        assertEquals("VEG", deserialized.step4.diet)
        assertEquals("Delhi", deserialized.step5.birthPlace)
        assertEquals("9876543210", deserialized.step6.phone)
    }
}

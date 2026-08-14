package com.asha.worker.ai

import com.asha.worker.ai.HealthDataParser.Symptom
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HealthDataParserTest {

    private val parser = HealthDataParser()

    @Test
    fun feverWithNameAgeDuration() {
        val v = parser.parse("सुनीता का बेटा तीन साल बुखार तीन दिन से")
        assertEquals("सुनीता", v.patientName)
        assertEquals(36, v.ageMonths)
        assertEquals(3, v.ageYears)
        assertEquals(Symptom.FEVER, v.symptomCode)
        assertEquals(3, v.durationDays)
    }

    @Test
    fun diarrheaNoPossessiveFallsBackToFirstWord() {
        val v = parser.parse("अमन को पांच साल दस्त दो दिन से")
        assertEquals("अमन", v.patientName)
        assertEquals(60, v.ageMonths)
        assertEquals(Symptom.DIARRHEA, v.symptomCode)
        assertEquals(2, v.durationDays)
    }

    @Test
    fun devanagariDigitsAreFolded() {
        val v = parser.parse("मीना का बच्चा २ साल खांसी ५ दिन से")
        assertEquals("मीना", v.patientName)
        assertEquals(24, v.ageMonths)
        assertEquals(Symptom.COUGH, v.symptomCode)
        assertEquals(5, v.durationDays)
    }

    @Test
    fun infantAgeExpressedInMonths() {
        val v = parser.parse("राधा का बेटा छह महीने का बुखार दो दिन से")
        assertEquals("राधा", v.patientName)
        assertEquals(6, v.ageMonths)
        assertEquals(0, v.ageYears)
        assertEquals(Symptom.FEVER, v.symptomCode)
        assertEquals(2, v.durationDays)
    }

    @Test
    fun weeksConvertToDays() {
        val v = parser.parse("सीता का बेटा एक हफ्ते से खांसी")
        assertEquals("सीता", v.patientName)
        assertNull(v.ageMonths)
        assertEquals(Symptom.COUGH, v.symptomCode)
        assertEquals(7, v.durationDays)
    }

    @Test
    fun specificPainBeatsGenericPain() {
        val v = parser.parse("गीता का बेटा पेट दर्द दो दिन से")
        assertEquals(Symptom.ABDOMINAL_PAIN, v.symptomCode)
    }

    @Test
    fun breathingDifficultyIsDetected() {
        val v = parser.parse("बच्चे को सांस लेने में दिक्कत")
        assertEquals(Symptom.BREATHING, v.symptomCode)
    }
}

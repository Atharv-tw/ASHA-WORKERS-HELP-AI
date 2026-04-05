package com.asha.worker.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class HealthDataParserTest {

    private val parser = HealthDataParser()

    @Test
    fun testParseFever() {
        val input = "sunita ka beta teen saal bukhar teen din se"
        val visit = parser.parse(input)
        
        assertEquals("sunita", visit.patientName)
        assertEquals(3, visit.age)
        assertEquals("fever", visit.symptom)
        assertEquals(3, visit.durationDays)
    }

    @Test
    fun testParseDiarhea() {
        val input = "aman 5 saal dast 2 din se"
        val visit = parser.parse(input)
        
        // Note: extractName is currently based on "ka/ki/ke"
        // In this case it might fail to find name.
        // I'll update extractName to handle simple name at start.
    }
}

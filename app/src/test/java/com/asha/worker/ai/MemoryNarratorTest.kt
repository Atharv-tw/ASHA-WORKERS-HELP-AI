package com.asha.worker.ai

import com.asha.worker.ai.data.FollowUpTask
import com.asha.worker.ai.data.FollowUpType
import com.asha.worker.ai.data.Patient
import com.asha.worker.ai.data.RecallResult
import com.asha.worker.ai.data.Visit
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoryNarratorTest {

    private fun patient() = Patient(id = 1, householdId = 1, name = "राजू", phoneticKey = "रज")

    @Test fun noRecordFound() {
        val msg = MemoryNarrator.narrate("राजू", null)
        assertTrue(msg.contains("कोई रिकॉर्ड नहीं"))
    }

    @Test fun lastVisitSpokenWithSymptomAndDuration() {
        val visit = Visit(id = 1, patientId = 1, symptomCode = "FEVER", durationDays = 3, referral = true)
        val msg = MemoryNarrator.narrate("राजू", RecallResult(patient(), visit, emptyList(), emptyList()))
        assertTrue(msg.contains("बुखार"))
        assertTrue(msg.contains("3 दिन"))
        assertTrue(msg.contains("रेफ़र"))
    }

    @Test fun pendingFollowUpMentioned() {
        val visit = Visit(id = 1, patientId = 1, symptomCode = "COUGH")
        val task = FollowUpTask(id = 1, patientId = 1, type = FollowUpType.SYMPTOM, dueMillis = 0, reason = "x")
        val msg = MemoryNarrator.narrate("राजू", RecallResult(patient(), visit, listOf(task), emptyList()))
        assertTrue(msg.contains("फ़ॉलो-अप"))
    }
}

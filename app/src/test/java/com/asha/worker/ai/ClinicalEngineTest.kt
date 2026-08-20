package com.asha.worker.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClinicalEngineTest {

    private val engine = ClinicalEngine()

    private fun visit(sym: String?, ageMonths: Int? = null, dur: Int? = null, name: String? = "राजू") =
        HealthVisit(patientName = name, ageMonths = ageMonths, symptomCode = sym, durationDays = dur)

    @Test fun convulsionRefers() {
        val g = engine.evaluate(visit("CONVULSION"))
        assertTrue(g.referral)
        assertEquals(Guidance.Severity.REFER, g.severity)
    }

    @Test fun breathingRefers() {
        assertTrue(engine.evaluate(visit("BREATHING_DIFFICULTY")).referral)
    }

    @Test fun under5FeverThreeDaysRefersForMalaria() {
        val g = engine.evaluate(visit("FEVER", ageMonths = 36, dur = 3))
        assertTrue(g.referral)
        assertTrue(g.message.contains("मलेरिया"))
    }

    @Test fun feverThreeDaysUnknownAgeStillRefers() {
        assertTrue(engine.evaluate(visit("FEVER", ageMonths = null, dur = 3)).referral)
    }

    @Test fun youngInfantFeverRefers() {
        assertTrue(engine.evaluate(visit("FEVER", ageMonths = 1, dur = 1)).referral)
    }

    @Test fun feverOneDayIsHomeCareWithFollowUp() {
        val g = engine.evaluate(visit("FEVER", ageMonths = 60, dur = 1))
        assertFalse(g.referral)
        assertEquals(Guidance.Severity.HOME_CARE, g.severity)
        assertEquals(2, g.followUpDays)
    }

    @Test fun persistentDiarrheaRefers() {
        assertTrue(engine.evaluate(visit("DIARRHEA", ageMonths = 24, dur = 3)).referral)
    }

    @Test fun shortDiarrheaIsHomeCare() {
        assertFalse(engine.evaluate(visit("DIARRHEA", ageMonths = 24, dur = 1)).referral)
    }

    @Test fun noSymptomIsInfo() {
        assertEquals(Guidance.Severity.INFO, engine.evaluate(visit(null)).severity)
    }

    @Test fun patientNameIsTemplatedIntoMessage() {
        assertTrue(engine.evaluate(visit("FEVER", ageMonths = 60, dur = 1, name = "सुनीता")).message.contains("सुनीता"))
    }

    @Test fun loaderParsesRulesJsonWithNulls() {
        val json = """
            [{"id":"x","symptom":"FEVER","minDurationDays":3,"maxAgeMonths":null,
              "minAgeMonths":null,"severity":"REFER","referral":true,"followUpDays":0,
              "message":"{name} जाँच"}]
        """.trimIndent()
        val rules = ClinicalRules.parse(json)
        assertEquals(1, rules.size)
        assertEquals("FEVER", rules[0].symptom)
        assertEquals(3, rules[0].minDurationDays)
        assertNull(rules[0].maxAgeMonths)
    }
}

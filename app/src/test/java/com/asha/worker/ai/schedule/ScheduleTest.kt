package com.asha.worker.ai.schedule

import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduleTest {

    private val day = 24L * 60 * 60 * 1000

    @Test fun immunizationDueDatesFromDob() {
        val dob = 1_000_000_000_000L
        val list = ImmunizationScheduler.forPatient(patientId = 7, dobMillis = dob)
        val byCode = list.associateBy { it.vaccineCode }
        assertEquals(dob, byCode.getValue("BCG").dueMillis)
        assertEquals(dob + 42 * day, byCode.getValue("PENTA1").dueMillis)
        assertEquals(dob + 270 * day, byCode.getValue("MR1").dueMillis)
        assertEquals(7L, byCode.getValue("BCG").patientId)
    }

    @Test fun scheduleParsesFromJson() {
        val json = """[{"code":"BCG","label":"बीसीजी","dueAgeDays":0},
                       {"code":"PENTA1","label":"पेंटा","dueAgeDays":42}]"""
        val defs = ImmunizationSchedule.parse(json)
        assertEquals(2, defs.size)
        assertEquals(42, defs[1].dueAgeDays)
    }

    @Test fun expectedDeliveryIs280Days() {
        val lmp = 1_500_000_000_000L
        assertEquals(lmp + 280 * day, PregnancyTracker.expectedDeliveryMillis(lmp))
    }

    @Test fun gestationalWeeksComputed() {
        val now = 1_600_000_000_000L
        val lmp = now - 70 * day // 10 weeks ago
        assertEquals(10, PregnancyTracker.gestationalWeeks(lmp, now))
    }

    @Test fun ancFirstVisitAtTwelveWeeks() {
        val lmp = 1_500_000_000_000L
        val anc = PregnancyTracker.ancSchedule(lmp).toMap()
        assertEquals(lmp + 12 * 7 * day, anc.getValue("ANC1"))
    }
}

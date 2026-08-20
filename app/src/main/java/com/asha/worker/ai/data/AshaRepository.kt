package com.asha.worker.ai.data

import com.asha.worker.ai.HealthVisit
import com.asha.worker.ai.planner.PlanItem
import com.asha.worker.ai.schedule.ImmunizationSchedule
import com.asha.worker.ai.schedule.ImmunizationScheduler
import com.asha.worker.ai.schedule.PregnancyTracker
import com.asha.worker.ai.text.PhoneticKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Everything remembered about one patient, assembled for a recall/briefing. */
data class RecallResult(
    val patient: Patient,
    val lastVisit: Visit?,
    val openTasks: List<FollowUpTask>,
    val dueImmunizations: List<Immunization>
)

/**
 * Mediates all database access and keeps it off the main thread. The rest of the app
 * never touches Room directly.
 */
class AshaRepository(private val db: AppDatabase) {

    /**
     * Persist a spoken visit. Finds or creates the household + patient by phonetic key,
     * then stores the visit with the guidance that was given.
     * @return the new visit id.
     */
    suspend fun recordVisit(
        visit: HealthVisit,
        guidance: String,
        referral: Boolean = false,
        followUpMillis: Long? = null
    ): Long = withContext(Dispatchers.IO) {
        val patient = findOrCreatePatient(visit.patientName, visit.ageMonths, visit.pregnantGender())
        val visitId = db.visitDao().insert(
            Visit(
                patientId = patient.id,
                symptomCode = visit.symptomCode,
                durationDays = visit.durationDays,
                guidance = guidance,
                referral = referral,
                followUpMillis = followUpMillis
            )
        )
        if (followUpMillis != null) {
            db.followUpDao().insert(
                FollowUpTask(
                    patientId = patient.id,
                    type = FollowUpType.SYMPTOM,
                    dueMillis = followUpMillis,
                    reason = guidance
                )
            )
        }
        // Enrol a young child in the immunization schedule the first time we meet them.
        val ageMonths = visit.ageMonths
        if (ageMonths != null && ageMonths < 24 && db.immunizationDao().forPatient(patient.id).isEmpty()) {
            val dob = patient.dobMillis ?: (System.currentTimeMillis() - ageMonths * AVG_MONTH_MILLIS)
            db.immunizationDao().insertAll(ImmunizationScheduler.forPatient(patient.id, dob))
        }
        visitId
    }

    /** Mark a patient pregnant and lay out the four ANC visit reminders from the LMP. */
    suspend fun enrollPregnancy(patientId: Long, lmpMillis: Long) = withContext(Dispatchers.IO) {
        val p = db.patientDao().getById(patientId) ?: return@withContext
        db.patientDao().update(p.copy(pregnant = true, lmpMillis = lmpMillis))
        PregnancyTracker.ancSchedule(lmpMillis).forEach { (code, due) ->
            db.followUpDao().insert(
                FollowUpTask(patientId = patientId, type = FollowUpType.ANC, dueMillis = due, reason = code)
            )
        }
    }

    suspend fun dueImmunizations(byMillis: Long): List<Immunization> =
        withContext(Dispatchers.IO) { db.immunizationDao().dueBy(byMillis) }

    suspend fun openFollowUps(byMillis: Long): List<FollowUpTask> =
        withContext(Dispatchers.IO) { db.followUpDao().openDueBy(byMillis) }

    /** Ranked plan of everything due by [byMillis] (default: through tomorrow). */
    suspend fun todaysPlan(
        byMillis: Long = System.currentTimeMillis() + DAY_MILLIS
    ): List<PlanItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<PlanItem>()
        db.followUpDao().openDueBy(byMillis).forEach { t ->
            val name = db.patientDao().getById(t.patientId)?.name ?: UNKNOWN_NAME
            val reason = when (t.type) {
                FollowUpType.ANC -> "एएनसी जाँच"
                FollowUpType.SYMPTOM -> "फ़ॉलो-अप"
                else -> t.reason
            }
            items.add(PlanItem(name, reason, t.dueMillis))
        }
        db.immunizationDao().dueBy(byMillis).forEach { im ->
            val name = db.patientDao().getById(im.patientId)?.name ?: UNKNOWN_NAME
            items.add(PlanItem(name, "टीकाकरण — ${ImmunizationSchedule.labelFor(im.vaccineCode)}", im.dueMillis))
        }
        items.sortedBy { it.dueMillis }
    }

    /** Look up a household/patient by spoken name and gather their memory. */
    suspend fun recall(name: String): RecallResult? = withContext(Dispatchers.IO) {
        val key = PhoneticKey.of(name)
        val patient = db.patientDao().findByPhoneticKey(key).firstOrNull() ?: return@withContext null
        RecallResult(
            patient = patient,
            lastVisit = db.visitDao().lastForPatient(patient.id),
            openTasks = db.followUpDao().open().filter { it.patientId == patient.id },
            dueImmunizations = db.immunizationDao().forPatient(patient.id)
                .filter { it.status == Immunization.STATUS_DUE }
        )
    }

    suspend fun addFollowUp(task: FollowUpTask): Long =
        withContext(Dispatchers.IO) { db.followUpDao().insert(task) }

    suspend fun allPatients(): List<Patient> =
        withContext(Dispatchers.IO) { db.patientDao().getAll() }

    suspend fun visitsFor(patientId: Long): List<Visit> =
        withContext(Dispatchers.IO) { db.visitDao().forPatient(patientId) }

    // --- internals ------------------------------------------------------------

    private suspend fun findOrCreatePatient(
        rawName: String?,
        ageMonths: Int?,
        gender: String?
    ): Patient {
        val name = rawName?.takeIf { it.isNotBlank() } ?: UNKNOWN_NAME
        val key = PhoneticKey.of(name)
        val household = db.householdDao().findByPhoneticKey(key)
            ?: db.householdDao().getById(
                db.householdDao().insert(Household(headName = name, phoneticKey = key))
            )!!
        return db.patientDao().findByPhoneticKey(key).firstOrNull { it.householdId == household.id }
            ?: db.patientDao().getById(
                db.patientDao().insert(
                    Patient(
                        householdId = household.id,
                        name = name,
                        phoneticKey = key,
                        gender = gender,
                        dobMillis = ageMonths?.let { dobFromAgeMonths(it) }
                    )
                )
            )!!
    }

    private fun dobFromAgeMonths(months: Int): Long =
        System.currentTimeMillis() - months * AVG_MONTH_MILLIS

    companion object {
        private const val UNKNOWN_NAME = "अज्ञात"
        private const val AVG_MONTH_MILLIS = 2_629_746_000L // 30.44 days
        private const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}

/** Placeholder until the parser extracts gender; keeps the call site stable. */
private fun HealthVisit.pregnantGender(): String? = null

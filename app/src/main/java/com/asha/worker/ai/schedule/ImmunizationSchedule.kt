package com.asha.worker.ai.schedule

import android.content.Context
import android.util.Log
import com.asha.worker.ai.data.Immunization
import org.json.JSONArray

/** One vaccine and the age (days from birth) at which it is due. */
data class VaccineDef(val code: String, val label: String, val dueAgeDays: Int)

/**
 * India National Immunization Schedule (representative subset). Loadable from
 * assets/immunization_schedule.json; [DEFAULT] is the in-code fallback / test source.
 */
object ImmunizationSchedule {

    val DEFAULT: List<VaccineDef> = listOf(
        VaccineDef("BCG", "बीसीजी", 0),
        VaccineDef("OPV0", "ओपीवी-0", 0),
        VaccineDef("HEPB0", "हेपेटाइटिस बी जन्म खुराक", 0),
        VaccineDef("PENTA1", "पेंटावैलेंट-1", 42),   // 6 weeks
        VaccineDef("OPV1", "ओपीवी-1", 42),
        VaccineDef("PENTA2", "पेंटावैलेंट-2", 70),   // 10 weeks
        VaccineDef("OPV2", "ओपीवी-2", 70),
        VaccineDef("PENTA3", "पेंटावैलेंट-3", 98),   // 14 weeks
        VaccineDef("OPV3", "ओपीवी-3", 98),
        VaccineDef("MR1", "खसरा-रूबेला 1", 270),     // 9 months
        VaccineDef("VITA1", "विटामिन ए 1", 270),
        VaccineDef("DPTB1", "डीपीटी बूस्टर 1", 480), // ~16 months
        VaccineDef("MR2", "खसरा-रूबेला 2", 480)
    )

    fun load(context: Context): List<VaccineDef> = try {
        val json = context.assets.open("immunization_schedule.json").bufferedReader().use { it.readText() }
        parse(json).ifEmpty { DEFAULT }
    } catch (e: Exception) {
        Log.w("ImmunizationSchedule", "schedule asset unavailable; using built-in", e)
        DEFAULT
    }

    fun parse(json: String): List<VaccineDef> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            VaccineDef(o.getString("code"), o.getString("label"), o.getInt("dueAgeDays"))
        }
    }
}

/** Computes concrete due dates for a child from date of birth. */
object ImmunizationScheduler {

    private const val DAY_MILLIS = 24L * 60 * 60 * 1000

    /** All schedule entries as [Immunization] rows for [patientId], dated from [dobMillis]. */
    fun forPatient(
        patientId: Long,
        dobMillis: Long,
        schedule: List<VaccineDef> = ImmunizationSchedule.DEFAULT
    ): List<Immunization> = schedule.map { v ->
        Immunization(
            patientId = patientId,
            vaccineCode = v.code,
            dueMillis = dobMillis + v.dueAgeDays * DAY_MILLIS
        )
    }
}

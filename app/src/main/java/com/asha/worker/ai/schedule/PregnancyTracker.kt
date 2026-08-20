package com.asha.worker.ai.schedule

/**
 * Pregnancy maths from the last menstrual period (LMP): expected delivery date,
 * current gestational week, and the four antenatal-care (ANC) visit windows
 * recommended under the national schedule. Pure/testable.
 */
object PregnancyTracker {

    private const val DAY_MILLIS = 24L * 60 * 60 * 1000
    private const val GESTATION_DAYS = 280 // 40 weeks (Naegele's rule)

    /** Expected date of delivery. */
    fun expectedDeliveryMillis(lmpMillis: Long): Long = lmpMillis + GESTATION_DAYS * DAY_MILLIS

    /** Completed weeks of gestation as of [asOf]. */
    fun gestationalWeeks(lmpMillis: Long, asOf: Long = System.currentTimeMillis()): Int =
        ((asOf - lmpMillis) / DAY_MILLIS / 7).toInt().coerceAtLeast(0)

    /** ANC visit code -> recommended due date. */
    fun ancSchedule(lmpMillis: Long): List<Pair<String, Long>> = listOf(
        "ANC1" to lmpMillis + weeks(12), // within first trimester
        "ANC2" to lmpMillis + weeks(20),
        "ANC3" to lmpMillis + weeks(30),
        "ANC4" to lmpMillis + weeks(36)
    )

    private fun weeks(n: Int): Long = n.toLong() * 7 * DAY_MILLIS
}

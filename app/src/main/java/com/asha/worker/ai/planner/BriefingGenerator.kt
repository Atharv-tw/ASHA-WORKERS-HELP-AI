package com.asha.worker.ai.planner

/** One ranked entry in the day's plan. */
data class PlanItem(val patientName: String, val reason: String, val dueMillis: Long)

/**
 * Turns a ranked [PlanItem] list into a short spoken Devanagari morning briefing —
 * the "personal secretary" voice. Pure/testable.
 */
object BriefingGenerator {

    private const val MAX_SPOKEN = 5

    fun briefing(items: List<PlanItem>): String {
        if (items.isEmpty()) return "आज कोई ज़रूरी विज़िट नहीं है।"
        val sb = StringBuilder("आज की प्राथमिकता विज़िट: ")
        items.take(MAX_SPOKEN).forEachIndexed { i, item ->
            sb.append("${i + 1}. ${item.patientName} — ${item.reason}। ")
        }
        if (items.size > MAX_SPOKEN) sb.append("और ${items.size - MAX_SPOKEN} अन्य।")
        return sb.toString().trim()
    }
}

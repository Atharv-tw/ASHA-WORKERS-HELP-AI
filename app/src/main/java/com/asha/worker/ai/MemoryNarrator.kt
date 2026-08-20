package com.asha.worker.ai

import com.asha.worker.ai.data.RecallResult

/**
 * Turns a [RecallResult] into a short spoken Devanagari summary — the Memory Engine's voice.
 * Pure/testable: no Android or DB dependencies.
 */
object MemoryNarrator {

    fun narrate(name: String, result: RecallResult?): String {
        if (result == null) return "$name के नाम से कोई रिकॉर्ड नहीं मिला।"

        val parts = mutableListOf<String>()
        val visit = result.lastVisit
        if (visit != null) {
            val symptom = SymptomLabels.hindi(visit.symptomCode) ?: "समस्या"
            val duration = visit.durationDays?.let { " $it दिन से" } ?: ""
            parts.add("पिछली विज़िट में $symptom थी$duration।")
            if (visit.referral) parts.add("तब पीएचसी रेफ़र किया गया था।")
        } else {
            parts.add("अभी तक कोई विज़िट दर्ज नहीं है।")
        }
        if (result.openTasks.isNotEmpty()) {
            parts.add("${result.openTasks.size} फ़ॉलो-अप बाकी है।")
        }
        if (result.dueImmunizations.isNotEmpty()) {
            parts.add("टीकाकरण बाकी है।")
        }
        return (listOf("${result.patient.name}।") + parts).joinToString(" ")
    }
}

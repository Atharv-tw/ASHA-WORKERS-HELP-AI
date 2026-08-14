package com.asha.worker.ai

import com.asha.worker.ai.text.DevanagariNormalizer

/**
 * Structured data extracted from one spoken visit sentence.
 *
 * Age is stored canonically in months so the clinical rules can compare infants
 * and older children uniformly. [ageYears] is a convenience view.
 */
data class HealthVisit(
    val patientName: String? = null,
    val ageMonths: Int? = null,
    val symptomCode: String? = null,
    val durationDays: Int? = null,
    val rawText: String = ""
) {
    val ageYears: Int? get() = ageMonths?.div(12)
}

/**
 * Converts a Devanagari sentence from the recognizer into a [HealthVisit].
 *
 * Everything is deterministic (regex + dictionaries) so it runs fully offline and
 * is exhaustively unit-testable. Symptom vocabulary maps to canonical codes
 * (FEVER, COUGH, ...) that the clinical rule engine keys on.
 */
class HealthDataParser {

    /** Devanagari symptom phrase -> canonical code. Matched longest-phrase-first. */
    private val symptomEntries: List<Pair<String, String>> = listOf(
        "साँस" to Symptom.BREATHING, "सांस" to Symptom.BREATHING, "दम फूल" to Symptom.BREATHING,
        "पेट दर्द" to Symptom.ABDOMINAL_PAIN, "पेट में दर्द" to Symptom.ABDOMINAL_PAIN,
        "सिर दर्द" to Symptom.HEADACHE, "सरदर्द" to Symptom.HEADACHE,
        "बुखार" to Symptom.FEVER, "बुख़ार" to Symptom.FEVER, "ज्वर" to Symptom.FEVER, "ताप" to Symptom.FEVER,
        "खांसी" to Symptom.COUGH, "खाँसी" to Symptom.COUGH,
        "जुकाम" to Symptom.COLD, "जुक़ाम" to Symptom.COLD, "सर्दी" to Symptom.COLD, "नाक बह" to Symptom.COLD,
        "दस्त" to Symptom.DIARRHEA, "लूज मोशन" to Symptom.DIARRHEA, "लूज़ मोशन" to Symptom.DIARRHEA,
        "उल्टी" to Symptom.VOMITING, "उलटी" to Symptom.VOMITING, "वमन" to Symptom.VOMITING,
        "दाने" to Symptom.RASH, "चकत्ते" to Symptom.RASH, "रैश" to Symptom.RASH,
        "दौरा" to Symptom.CONVULSION, "झटके" to Symptom.CONVULSION,
        "कमजोर" to Symptom.WEAKNESS, "कमज़ोर" to Symptom.WEAKNESS, "सुस्त" to Symptom.WEAKNESS,
        "दर्द" to Symptom.PAIN
    ).sortedByDescending { it.first.length }

    private val numberWords: Map<String, Int> = mapOf(
        "एक" to 1, "दो" to 2, "तीन" to 3, "चार" to 4, "पांच" to 5, "पाँच" to 5,
        "छह" to 6, "छे" to 6, "सात" to 7, "आठ" to 8, "नौ" to 9, "दस" to 10,
        "ग्यारह" to 11, "बारह" to 12, "पंद्रह" to 15, "बीस" to 20, "पच्चीस" to 25, "तीस" to 30
    )

    private val relationWords = setOf(
        "बेटा", "बेटी", "बच्चा", "बच्ची", "पति", "पत्नी", "सास", "बहू",
        "माँ", "मां", "बाप", "लड़का", "लड़की", "घर"
    )

    private val unitWords = setOf(
        "साल", "वर्ष", "बरस", "दिन", "महीने", "महीना", "माह",
        "हफ्ते", "हफ्ता", "सप्ताह", "से", "को", "में", "का", "की", "के"
    )

    fun parse(text: String): HealthVisit {
        val norm = DevanagariNormalizer.normalize(text)
        return HealthVisit(
            patientName = extractName(norm),
            ageMonths = extractAgeMonths(norm),
            symptomCode = symptomEntries.firstOrNull { norm.contains(it.first) }?.second,
            durationDays = extractDurationDays(norm),
            rawText = text
        )
    }

    // --- number handling ------------------------------------------------------

    private fun numberPattern(): String {
        val words = numberWords.keys.sortedByDescending { it.length }.joinToString("|")
        return "(\\d+|$words)"
    }

    private fun valueOf(token: String): Int? = token.toIntOrNull() ?: numberWords[token]

    private fun numberBefore(text: String, unitAlternation: String): Int? {
        val m = Regex("${numberPattern()}\\s+($unitAlternation)").find(text) ?: return null
        return valueOf(m.groupValues[1])
    }

    // --- field extractors -----------------------------------------------------

    private fun extractAgeMonths(text: String): Int? {
        numberBefore(text, "साल|वर्ष|बरस")?.let { return it * 12 }
        // "X महीने का/की/के" => age expressed in months (infants)
        Regex("${numberPattern()}\\s+(?:महीने|महीना|माह)\\s+(?:का|की|के)")
            .find(text)?.let { return valueOf(it.groupValues[1]) }
        return null
    }

    private fun extractDurationDays(text: String): Int? {
        numberBefore(text, "दिन")?.let { return it }
        numberBefore(text, "हफ्ते|हफ्ता|सप्ताह|हफ़्ते")?.let { return it * 7 }
        // "X महीने से" => duration in months since
        Regex("${numberPattern()}\\s+(?:महीने|महीना|माह)\\s+से")
            .find(text)?.let { return valueOf(it.groupValues[1])?.times(30) }
        return null
    }

    private fun extractName(text: String): String? {
        // Name is what precedes the first possessive marker का / की / के.
        Regex("^(.*?)\\s+(?:का|की|के)(?:\\s|$)").find(text)?.let {
            val candidate = it.groupValues[1].trim()
            if (candidate.isNotEmpty()) return candidate
        }
        // Fallback: first word, if it isn't a number, unit, symptom or relation word.
        val first = text.split(" ").firstOrNull()?.trim().orEmpty()
        if (first.isNotEmpty() && valueOf(first) == null && !isKeyword(first)) return first
        return null
    }

    private fun isKeyword(w: String): Boolean =
        w in relationWords || w in unitWords || symptomEntries.any { it.first == w }

    /** Canonical symptom codes shared with the clinical rule engine. */
    object Symptom {
        const val FEVER = "FEVER"
        const val COUGH = "COUGH"
        const val COLD = "COLD"
        const val DIARRHEA = "DIARRHEA"
        const val VOMITING = "VOMITING"
        const val ABDOMINAL_PAIN = "ABDOMINAL_PAIN"
        const val HEADACHE = "HEADACHE"
        const val BREATHING = "BREATHING_DIFFICULTY"
        const val RASH = "RASH"
        const val CONVULSION = "CONVULSION"
        const val WEAKNESS = "WEAKNESS"
        const val PAIN = "PAIN"
    }
}

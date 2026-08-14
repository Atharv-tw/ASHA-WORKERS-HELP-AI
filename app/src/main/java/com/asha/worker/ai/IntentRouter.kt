package com.asha.worker.ai

import com.asha.worker.ai.text.DevanagariNormalizer

/** What the worker is trying to do with a spoken utterance. */
enum class Intent { RECORD_VISIT, RECALL, GUIDANCE_QUERY, DAILY_PLAN, UNKNOWN }

/**
 * Decides the [Intent] of a Devanagari utterance using deterministic keyword rules,
 * so one microphone button can serve four different jobs. Fully offline / testable.
 *
 * Precedence (first match wins):
 *   1. DAILY_PLAN     – "आज ... करना/जाना", "योजना", "प्लान"
 *   2. GUIDANCE_QUERY – general questions like "... तो क्या करें", "कैसे"
 *   3. RECALL         – asking about a household ("राजू का घर") with no symptom reported
 *   4. RECORD_VISIT   – a symptom is present
 *   5. RECALL         – a bare name; else UNKNOWN
 */
class IntentRouter(private val parser: HealthDataParser = HealthDataParser()) {

    private val planActionWords = listOf("करना", "काम", "जाना", "विजिट", "मुलाकात", "किसके", "किस")
    private val guidancePhrases = listOf("क्या करें", "क्या करूं", "क्या करूँ", "क्या करे", "क्या करना चाहिए", "तो क्या", "कैसे करें", "कैसे")
    private val recallWords = listOf("घर", "जानकारी", "बारे में", "याद", "पिछली", "इतिहास", "हिस्ट्री")

    fun classify(rawText: String): Intent {
        val t = DevanagariNormalizer.normalize(rawText)
        if (t.isBlank()) return Intent.UNKNOWN

        if ((t.contains("आज") && planActionWords.any { t.contains(it) }) ||
            t.contains("योजना") || t.contains("प्लान")
        ) return Intent.DAILY_PLAN

        if (guidancePhrases.any { t.contains(it) }) return Intent.GUIDANCE_QUERY

        val hasSymptom = parser.parse(t).symptomCode != null

        if (!hasSymptom && recallWords.any { t.contains(it) }) return Intent.RECALL
        if (hasSymptom) return Intent.RECORD_VISIT
        if (t.split(" ").size <= 2) return Intent.RECALL

        return Intent.UNKNOWN
    }
}

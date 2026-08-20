package com.asha.worker.ai

import com.asha.worker.ai.HealthDataParser.Symptom

/** Canonical symptom code -> spoken Hindi (Devanagari) label. */
object SymptomLabels {
    private val hindi = mapOf(
        Symptom.FEVER to "बुखार",
        Symptom.COUGH to "खांसी",
        Symptom.COLD to "जुकाम",
        Symptom.DIARRHEA to "दस्त",
        Symptom.VOMITING to "उल्टी",
        Symptom.ABDOMINAL_PAIN to "पेट दर्द",
        Symptom.HEADACHE to "सिर दर्द",
        Symptom.BREATHING to "साँस की तकलीफ़",
        Symptom.RASH to "दाने",
        Symptom.CONVULSION to "दौरा",
        Symptom.WEAKNESS to "कमज़ोरी",
        Symptom.PAIN to "दर्द"
    )

    fun hindi(code: String?): String? = code?.let { hindi[it] }
}

package com.asha.worker.ai

class ClinicalEngine {

    fun getGuidance(visit: HealthVisit): String {
        val name = visit.patientName ?: "बच्चे"
        val symptom = visit.symptomCode
        val duration = visit.durationDays ?: 0

        return when {
            symptom == HealthDataParser.Symptom.FEVER && duration >= 3 ->
                "$name को तीन दिन से बुखार है। उसे तुरंत पीएचसी ले जाइए।"
            symptom == HealthDataParser.Symptom.FEVER ->
                "$name को बुखार है। पैरासिटामोल दीजिए और कल तक इंतज़ार कीजिए।"
            symptom == HealthDataParser.Symptom.DIARRHEA ->
                "$name को दस्त है। ओआरएस का घोल पिलाइए और साफ़ पानी दीजिए।"
            symptom == HealthDataParser.Symptom.COUGH ->
                "$name को खांसी है। गरम पानी पिलाइए।"
            else ->
                "ठीक है। मैंने जानकारी दर्ज कर ली है।"
        }
    }
}

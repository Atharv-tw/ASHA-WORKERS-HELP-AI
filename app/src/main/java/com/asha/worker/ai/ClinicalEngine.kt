package com.asha.worker.ai

/**
 * Structured decision-support output. NOT a diagnosis — conservative, referral-biased
 * guidance derived from national community-health guidelines (IMNCI / HBNC).
 */
data class Guidance(
    val message: String,
    val referral: Boolean,
    val severity: String,
    val followUpDays: Int?
) {
    object Severity {
        const val REFER = "REFER"
        const val HOME_CARE = "HOME_CARE"
        const val INFO = "INFO"
    }
}

/** One condition -> guidance rule. Null condition fields mean "don't care". */
data class ClinicalRule(
    val id: String,
    val symptom: String?,
    val minDurationDays: Int?,
    val maxAgeMonths: Int?,
    val minAgeMonths: Int?,
    val severity: String,
    val referral: Boolean,
    val followUpDays: Int?,
    val message: String
)

/**
 * Evaluates a [HealthVisit] against an ordered rule list (first match wins), so
 * danger-sign rules take precedence over home-care rules. Rules are data (loadable
 * from assets/rules.json); [DEFAULT_RULES] is the in-code fallback and test source.
 */
class ClinicalEngine(private val rules: List<ClinicalRule> = DEFAULT_RULES) {

    fun evaluate(visit: HealthVisit): Guidance {
        val rule = rules.firstOrNull { matches(it, visit) }
            ?: return Guidance(
                "ठीक है। मैंने जानकारी दर्ज कर ली है।",
                referral = false,
                severity = Guidance.Severity.INFO,
                followUpDays = null
            )
        return Guidance(fill(rule.message, visit), rule.referral, rule.severity, rule.followUpDays)
    }

    /** Backwards-compatible convenience used by simple callers. */
    fun getGuidance(visit: HealthVisit): String = evaluate(visit).message

    private fun matches(r: ClinicalRule, v: HealthVisit): Boolean {
        if (r.symptom != null && r.symptom != v.symptomCode) return false
        if (r.minDurationDays != null && (v.durationDays ?: 0) < r.minDurationDays) return false
        if (r.maxAgeMonths != null && (v.ageMonths == null || v.ageMonths > r.maxAgeMonths)) return false
        if (r.minAgeMonths != null && (v.ageMonths == null || v.ageMonths < r.minAgeMonths)) return false
        return true
    }

    private fun fill(message: String, v: HealthVisit): String {
        val name = v.patientName?.takeIf { it.isNotBlank() } ?: "बच्चे"
        val symptom = SymptomLabels.hindi(v.symptomCode) ?: "समस्या"
        return message.replace("{name}", name).replace("{symptom}", symptom)
    }

    companion object {
        /**
         * Ordered danger-sign-first rule set. Conservative: uncertain cases bias to referral.
         * Grounded in IMNCI/HBNC red flags (convulsions, fast breathing, young-infant fever,
         * under-5 prolonged fever = malaria risk, persistent diarrhea, dehydration).
         */
        val DEFAULT_RULES: List<ClinicalRule> = listOf(
            ClinicalRule("convulsion", "CONVULSION", null, null, null,
                Guidance.Severity.REFER, true, 0,
                "{name} को दौरे पड़ रहे हैं। यह गंभीर है — तुरंत पीएचसी या अस्पताल ले जाइए।"),
            ClinicalRule("breathing", "BREATHING_DIFFICULTY", null, null, null,
                Guidance.Severity.REFER, true, 0,
                "{name} को साँस लेने में तकलीफ़ है। निमोनिया का ख़तरा हो सकता है — तुरंत पीएचसी ले जाइए।"),
            ClinicalRule("young_infant_fever", "FEVER", null, 2, null,
                Guidance.Severity.REFER, true, 0,
                "{name} बहुत छोटा है और बुखार है। तुरंत पीएचसी ले जाइए।"),
            ClinicalRule("fever_under5_3d", "FEVER", 3, 60, null,
                Guidance.Severity.REFER, true, 0,
                "{name} को तीन दिन से बुखार है और उम्र पाँच साल से कम है। मलेरिया की जाँच के लिए तुरंत पीएचसी ले जाइए।"),
            ClinicalRule("fever_3d_any", "FEVER", 3, null, null,
                Guidance.Severity.REFER, true, 0,
                "{name} को तीन दिन से बुखार है। जाँच के लिए पीएचसी ले जाइए।"),
            ClinicalRule("persistent_diarrhea", "DIARRHEA", 3, null, null,
                Guidance.Severity.REFER, true, 0,
                "{name} को तीन दिन से दस्त है। पीएचसी ले जाइए और बीच में ओआरएस देते रहिए।"),
            ClinicalRule("vomiting_child", "VOMITING", 2, 60, null,
                Guidance.Severity.REFER, true, 1,
                "{name} को लगातार उल्टी हो रही है, पानी की कमी हो सकती है। पीएचसी ले जाइए और ओआरएस पिलाइए।"),
            ClinicalRule("fever_home", "FEVER", null, null, null,
                Guidance.Severity.HOME_CARE, false, 2,
                "{name} को बुखार है। पैरासिटामोल दीजिए, पानी पिलाते रहिए और दो दिन बाद फिर देखिए।"),
            ClinicalRule("diarrhea_home", "DIARRHEA", null, null, null,
                Guidance.Severity.HOME_CARE, false, 2,
                "{name} को दस्त है। ओआरएस का घोल और ज़िंक दीजिए, साफ़ पानी पिलाइए।"),
            ClinicalRule("cough", "COUGH", null, null, null,
                Guidance.Severity.HOME_CARE, false, 3,
                "{name} को खांसी है। गरम पानी और आराम दीजिए। साँस तेज़ हो तो पीएचसी ले जाइए।"),
            ClinicalRule("cold", "COLD", null, null, null,
                Guidance.Severity.HOME_CARE, false, 3,
                "{name} को जुकाम है। आराम और गरम तरल दीजिए।"),
            ClinicalRule("weakness", "WEAKNESS", null, null, null,
                Guidance.Severity.HOME_CARE, false, 3,
                "{name} को कमज़ोरी है। पौष्टिक आहार दीजिए और ध्यान रखिए।")
        )
    }
}

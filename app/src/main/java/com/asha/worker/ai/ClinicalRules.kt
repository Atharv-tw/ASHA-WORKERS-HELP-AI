package com.asha.worker.ai

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * Loads clinical rules from assets/rules.json so guidance can be tuned without
 * recompiling. Falls back to [ClinicalEngine.DEFAULT_RULES] if the asset is missing
 * or malformed, so the engine is never left without rules.
 */
object ClinicalRules {

    fun load(context: Context): List<ClinicalRule> = try {
        val json = context.assets.open("rules.json").bufferedReader().use { it.readText() }
        parse(json).ifEmpty { ClinicalEngine.DEFAULT_RULES }
    } catch (e: Exception) {
        Log.w("ClinicalRules", "rules.json unavailable; using built-in rules", e)
        ClinicalEngine.DEFAULT_RULES
    }

    /** Exposed for tests. */
    fun parse(json: String): List<ClinicalRule> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            ClinicalRule(
                id = o.getString("id"),
                symptom = o.nullableString("symptom"),
                minDurationDays = o.nullableInt("minDurationDays"),
                maxAgeMonths = o.nullableInt("maxAgeMonths"),
                minAgeMonths = o.nullableInt("minAgeMonths"),
                severity = o.getString("severity"),
                referral = o.getBoolean("referral"),
                followUpDays = o.nullableInt("followUpDays"),
                message = o.getString("message")
            )
        }
    }

    private fun JSONObject.nullableString(key: String): String? =
        if (!has(key) || isNull(key)) null else getString(key)

    private fun JSONObject.nullableInt(key: String): Int? =
        if (!has(key) || isNull(key)) null else getInt(key)
}

package com.asha.worker.ai

data class HealthVisit(
    val patientName: String? = null,
    val age: Int? = null,
    val symptom: String? = null,
    val durationDays: Int? = null
)

class HealthDataParser {

    private val symptomMap = mapOf(
        "bukhar" to "fever",
        "jwar" to "fever",
        "tap" to "fever",
        "khansi" to "cough",
        "dast" to "diarrhea",
        "pet dard" to "stomach ache",
        "ultice" to "vomiting",
        "vomit" to "vomiting"
    )

    private val numberMap = mapOf(
        "ek" to 1, "do" to 2, "teen" to 3, "chaar" to 4, "paanch" to 5,
        "cheh" to 6, "saat" to 7, "aath" to 8, "nau" to 9, "dus" to 10
    )

    fun parse(text: String): HealthVisit {
        val lowercaseText = text.lowercase()
        
        val age = extractNumberBefore(lowercaseText, "saal")
        val duration = extractNumberBefore(lowercaseText, "din")
        
        var foundSymptom: String? = null
        for ((hindi, english) in symptomMap) {
            if (lowercaseText.contains(hindi)) {
                foundSymptom = english
                break
            }
        }

        // Simple name extraction: everything before the first comma or "ka" or "ki"
        val name = extractName(lowercaseText)

        return HealthVisit(
            patientName = name,
            age = age,
            symptom = foundSymptom,
            durationDays = duration
        )
    }

    private fun extractNumberBefore(text: String, keyword: String): Int? {
        val regex = Regex("(\\d+|${numberMap.keys.joinToString("|")})\\s+$keyword")
        val match = regex.find(text)
        return match?.let {
            val value = it.groupValues[1]
            value.toIntOrNull() ?: numberMap[value]
        }
    }

    private fun extractName(text: String): String? {
        // 1. Try to find name before "ka" or "ki"
        val matchKa = Regex("^(.*?)\\s+(ka|ki|ke)").find(text)
        if (matchKa != null) {
            return matchKa.groupValues[1].trim()
        }
        
        // 2. Fallback: Take first word if it's not a number or keyword
        val words = text.split(" ")
        if (words.isNotEmpty()) {
            val firstWord = words[0]
            if (firstWord !in numberMap.keys && firstWord.toIntOrNull() == null) {
                return firstWord
            }
        }
        
        return null
    }
}

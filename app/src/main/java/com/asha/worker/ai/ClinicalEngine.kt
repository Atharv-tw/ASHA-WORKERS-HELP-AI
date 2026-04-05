package com.asha.worker.ai

class ClinicalEngine {

    fun getGuidance(visit: HealthVisit): String {
        val name = visit.patientName ?: "Bachche"
        val symptom = visit.symptom
        val duration = visit.durationDays ?: 0

        return when {
            symptom == "fever" && duration >= 3 -> 
                "$name ko teen din se bukhar hai. Use turant PHC le jaaiye."
            symptom == "fever" -> 
                "$name ko bukhar hai. Use paracetamol dijiye aur kal tak intezar kijiye."
            symptom == "diarrhea" -> 
                "$name ko dast hai. Use ORS ka ghol pilaiye aur saaf pani dijiye."
            symptom == "cough" -> 
                "$name ko khansi hai. Use garam pani pilaiye."
            else -> 
                "Theek hai. Maine jankari darj kar li hai."
        }
    }
}

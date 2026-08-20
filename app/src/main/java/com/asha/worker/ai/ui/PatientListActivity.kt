package com.asha.worker.ai.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.asha.worker.ai.R
import com.asha.worker.ai.SymptomLabels
import com.asha.worker.ai.data.AppDatabase
import com.asha.worker.ai.data.AshaRepository
import kotlinx.coroutines.launch

/** Lists every household/patient with a one-line memory of their last visit. */
class PatientListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        title = getString(R.string.title_patients)

        val listText = findViewById<TextView>(R.id.listText)
        val repo = AshaRepository(AppDatabase.get(this))
        lifecycleScope.launch {
            val patients = repo.allPatients()
            listText.text = if (patients.isEmpty()) {
                getString(R.string.empty_patients)
            } else {
                // map is inline, so the suspend repo call is allowed here; join afterwards.
                val lines = patients.map { p ->
                    val last = repo.visitsFor(p.id).firstOrNull()
                    val summary = when {
                        last == null -> "कोई विज़िट नहीं"
                        else -> "पिछली: " + (SymptomLabels.hindi(last.symptomCode) ?: "—")
                    }
                    "${p.name} — $summary"
                }
                lines.joinToString("\n\n")
            }
        }
    }
}

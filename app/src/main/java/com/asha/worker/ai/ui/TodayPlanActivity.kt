package com.asha.worker.ai.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.asha.worker.ai.R
import com.asha.worker.ai.data.AppDatabase
import com.asha.worker.ai.data.AshaRepository
import kotlinx.coroutines.launch

/** Shows the ranked plan of everything due today. */
class TodayPlanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        title = getString(R.string.title_today_plan)

        val listText = findViewById<TextView>(R.id.listText)
        val repo = AshaRepository(AppDatabase.get(this))
        lifecycleScope.launch {
            val plan = repo.todaysPlan()
            listText.text = if (plan.isEmpty()) {
                getString(R.string.empty_plan)
            } else {
                plan.mapIndexed { i, item -> "${i + 1}. ${item.patientName} — ${item.reason}" }
                    .joinToString("\n\n")
            }
        }
    }
}

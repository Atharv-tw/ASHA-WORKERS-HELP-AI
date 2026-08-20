package com.asha.worker.ai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.asha.worker.ai.data.AppDatabase
import com.asha.worker.ai.data.AshaRepository
import com.asha.worker.ai.text.DevanagariNormalizer
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : AppCompatActivity(), VoskService.VoskListener {

    private lateinit var statusText: TextView
    private lateinit var recordButton: Button

    private lateinit var voskService: VoskService
    private lateinit var ttsService: TtsService
    private lateinit var repo: AshaRepository

    private val parser = HealthDataParser()
    private val router = IntentRouter(parser)
    private lateinit var clinicalEngine: ClinicalEngine

    private var isRecording = false

    companion object {
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
        private const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        recordButton = findViewById(R.id.recordButton)

        voskService = VoskService(this, this)
        ttsService = TtsService(this)
        repo = AshaRepository(AppDatabase.get(this))
        clinicalEngine = ClinicalEngine(ClinicalRules.load(this))

        val permissionCheck =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSIONS_REQUEST_RECORD_AUDIO
            )
        } else {
            initVosk()
        }

        recordButton.setOnClickListener { toggleRecording() }
    }

    private fun initVosk() {
        statusText.text = getString(R.string.status_initializing)
        voskService.initModel()
    }

    private fun toggleRecording() {
        if (isRecording) {
            voskService.stopListening()
            recordButton.text = getString(R.string.btn_record)
            isRecording = false
        } else {
            voskService.startListening()
            recordButton.text = getString(R.string.btn_stop)
            isRecording = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initVosk()
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // --- VoskListener ---------------------------------------------------------

    override fun onResult(hypothesis: String) {
        val text = extract(hypothesis, "text")
        if (text.isNotEmpty()) route(text)
    }

    override fun onPartialResult(hypothesis: String) {
        val text = extract(hypothesis, "partial")
        if (text.isNotEmpty()) statusText.text = getString(R.string.status_listening, text)
    }

    override fun onError(e: Exception) {
        statusText.text = getString(R.string.status_error, e.message ?: "")
        isRecording = false
        recordButton.text = getString(R.string.btn_record)
    }

    override fun onReady() {
        statusText.text = getString(R.string.status_ready)
        recordButton.isEnabled = true
    }

    // --- routing --------------------------------------------------------------

    private fun route(text: String) {
        when (router.classify(text)) {
            Intent.RECORD_VISIT -> recordVisit(text)
            Intent.GUIDANCE_QUERY -> handleGuidance(text)
            Intent.RECALL -> handleRecall(text)
            Intent.DAILY_PLAN -> speakAndShow(getString(R.string.plan_not_ready))
            Intent.UNKNOWN -> speakAndShow(getString(R.string.not_understood))
        }
    }

    private fun recordVisit(text: String) {
        val visit = parser.parse(text)
        val guidance = clinicalEngine.evaluate(visit)
        speakAndShow(guidance.message)
        val followUpMillis = guidance.followUpDays?.let { System.currentTimeMillis() + it * DAY_MILLIS }
        lifecycleScope.launch {
            repo.recordVisit(visit, guidance.message, guidance.referral, followUpMillis)
        }
    }

    private fun handleGuidance(text: String) {
        speakAndShow(clinicalEngine.evaluate(parser.parse(text)).message)
    }

    private fun handleRecall(text: String) {
        val name = parser.parse(text).patientName ?: DevanagariNormalizer.normalize(text)
        lifecycleScope.launch {
            val result = repo.recall(name)
            speakAndShow(MemoryNarrator.narrate(name, result))
        }
    }

    private fun speakAndShow(message: String) {
        statusText.text = message
        ttsService.speak(message)
    }

    private fun extract(json: String, key: String): String = try {
        JSONObject(json).getString(key)
    } catch (e: Exception) {
        json
    }

    override fun onDestroy() {
        super.onDestroy()
        voskService.destroy()
        ttsService.shutdown()
    }
}

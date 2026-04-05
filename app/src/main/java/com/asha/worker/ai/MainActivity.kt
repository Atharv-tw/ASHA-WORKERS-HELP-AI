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
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : AppCompatActivity(), VoskService.VoskListener {

    private lateinit var statusText: TextView
    private lateinit var recordButton: Button
    
    private lateinit var voskService: VoskService
    private lateinit var parser: HealthDataParser
    private lateinit var db: AppDatabase
    private lateinit var clinicalEngine: ClinicalEngine
    private lateinit var ttsService: TtsService

    private var isRecording = false

    companion object {
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        recordButton = findViewById(R.id.recordButton)

        voskService = VoskService(this, this)
        parser = HealthDataParser()
        db = AppDatabase.getDatabase(this)
        clinicalEngine = ClinicalEngine()
        ttsService = TtsService(this)

        // Check permissions
        val permissionCheck = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSIONS_REQUEST_RECORD_AUDIO)
        } else {
            initVosk()
        }

        recordButton.setOnClickListener {
            toggleRecording()
        }
    }

    private fun initVosk() {
        statusText.text = "Initializing Hindi Voice Model..."
        voskService.initModel()
    }

    private fun toggleRecording() {
        if (isRecording) {
            voskService.stopListening()
            recordButton.text = "Record Visit"
            isRecording = false
        } else {
            voskService.startListening()
            recordButton.text = "Stop Recording"
            isRecording = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initVosk()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onResult(hypothesis: String) {
        // Vosk result is JSON, extract text
        val text = try {
            JSONObject(hypothesis).getString("text")
        } catch (e: Exception) {
            hypothesis
        }

        if (text.isNotEmpty()) {
            processVisit(text)
        }
    }

    private fun processVisit(text: String) {
        statusText.text = "Processing: $text"
        
        // 1. Parse Data
        val visit = parser.parse(text)
        
        // 2. Save to DB
        lifecycleScope.launch {
            db.visitDao().insert(
                VisitRecord(
                    patientName = visit.patientName ?: "Unknown",
                    age = visit.age,
                    symptom = visit.symptom,
                    durationDays = visit.durationDays
                )
            )
        }

        // 3. Get Guidance & Speak
        val guidance = clinicalEngine.getGuidance(visit)
        statusText.text = "Guidance: $guidance"
        ttsService.speak(guidance)
    }

    override fun onPartialResult(hypothesis: String) {
        val text = try {
            JSONObject(hypothesis).getString("partial")
        } catch (e: Exception) {
            hypothesis
        }
        if (text.isNotEmpty()) {
            statusText.text = "Listening: $text"
        }
    }

    override fun onError(e: Exception) {
        statusText.text = "Error: ${e.message}"
        isRecording = false
        recordButton.text = "Record Visit"
    }

    override fun onReady() {
        statusText.text = "Ready to record (Hindi)"
        recordButton.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        voskService.stopListening()
        ttsService.shutdown()
    }
}

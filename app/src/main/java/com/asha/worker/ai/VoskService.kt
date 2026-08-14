package com.asha.worker.ai

import android.content.Context
import android.util.Log
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.io.IOException

/**
 * Wraps the offline Vosk speech-to-text engine.
 *
 * Lifecycle:
 *   initModel()  -> unpacks the bundled Hindi model from assets (once), then onReady()
 *   startListening()/stopListening() -> live microphone recognition
 *   destroy()    -> releases the recognizer and model (call from Activity.onDestroy)
 *
 * The Hindi model emits Devanagari text (e.g. "बुखार"), which downstream parsing expects.
 */
class VoskService(private val context: Context, private val listener: VoskListener) {

    interface VoskListener {
        fun onResult(hypothesis: String)
        fun onPartialResult(hypothesis: String)
        fun onError(e: Exception)
        fun onReady()
    }

    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var speechService: SpeechService? = null
    private var listening = false

    val isModelReady: Boolean get() = model != null
    val isListening: Boolean get() = listening

    /** Unpacks assets/model-hi -> filesDir/model and loads it off the UI thread. */
    fun initModel() {
        if (model != null) {
            listener.onReady()
            return
        }
        StorageService.unpack(
            context, "model-hi", "model",
            { loaded ->
                model = loaded
                listener.onReady()
            },
            { exception ->
                Log.e(TAG, "Vosk model load failed", exception)
                listener.onError(exception)
            }
        )
    }

    fun startListening() {
        val m = model
        if (m == null) {
            listener.onError(IllegalStateException("Voice model not loaded yet"))
            return
        }
        if (listening) return
        try {
            val rec = Recognizer(m, SAMPLE_RATE)
            val service = SpeechService(rec, SAMPLE_RATE)
            service.startListening(object : RecognitionListener {
                override fun onPartialResult(hypothesis: String) = listener.onPartialResult(hypothesis)
                override fun onResult(hypothesis: String) = listener.onResult(hypothesis)
                override fun onFinalResult(hypothesis: String) = listener.onResult(hypothesis)
                override fun onError(exception: Exception) {
                    listening = false
                    listener.onError(exception)
                }
                override fun onTimeout() { /* no-op: we control start/stop manually */ }
            })
            recognizer = rec
            speechService = service
            listening = true
        } catch (e: IOException) {
            listener.onError(e)
        }
    }

    fun stopListening() {
        speechService?.stop()
        speechService?.shutdown()
        speechService = null
        recognizer?.close()
        recognizer = null
        listening = false
    }

    /** Release all native resources. Safe to call multiple times. */
    fun destroy() {
        stopListening()
        model?.close()
        model = null
    }

    companion object {
        private const val TAG = "VoskService"
        private const val SAMPLE_RATE = 16000.0f
    }
}

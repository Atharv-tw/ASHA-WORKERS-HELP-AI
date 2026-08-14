package com.asha.worker.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * Wraps Android Text-To-Speech for Hindi output.
 *
 * Robustness over the naive version:
 *  - queues a phrase requested before the engine finishes initializing, then speaks it
 *  - reports whether the Hindi voice data is actually installed (via [onStatus])
 *  - assigns utterance ids so callers could track completion if needed
 */
class TtsService(
    context: Context,
    private val onStatus: ((Status) -> Unit)? = null
) : TextToSpeech.OnInitListener {

    enum class Status { READY, HINDI_MISSING, INIT_FAILED }

    private val tts: TextToSpeech = TextToSpeech(context, this)
    private var isReady = false
    private var pending: String? = null
    private var utteranceCounter = 0

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            Log.e(TAG, "TTS init failed: $status")
            onStatus?.invoke(Status.INIT_FAILED)
            return
        }
        val result = tts.setLanguage(HINDI)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Hindi voice data unavailable (result=$result)")
            onStatus?.invoke(Status.HINDI_MISSING)
            // Fall back so the app is still audible; still mark ready.
        }
        isReady = true
        onStatus?.invoke(Status.READY)
        pending?.let { speak(it); pending = null }
    }

    /** Speak [text]. If the engine isn't ready yet, the latest phrase is queued. */
    fun speak(text: String) {
        if (text.isBlank()) return
        if (!isReady) {
            pending = text
            return
        }
        val id = "asha-${utteranceCounter++}"
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
    }

    fun stop() {
        tts.stop()
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }

    companion object {
        private const val TAG = "TtsService"
        private val HINDI = Locale("hi", "IN")
    }
}

package com.asha.worker.ai

import android.content.Context
import android.util.Log
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.SpeechService
import org.vosk.android.SpeechStreamService
import org.vosk.android.StorageService
import java.io.IOException

class VoskService(private val context: Context, private val listener: VoskListener) {

    interface VoskListener {
        fun onResult(hypothesis: String)
        fun onPartialResult(hypothesis: String)
        fun onError(e: Exception)
        fun onReady()
    }

    private var model: Model? = null
    private var speechService: SpeechService? = null

    fun initModel() {
        StorageService.unpack(context, "model-hi", "model",
            { model ->
                this.model = model
                listener.onReady()
            },
            { exception ->
                listener.onError(exception)
            })
    }

    fun startListening() {
        if (model == null) return

        try {
            val rec = Recognizer(model, 16000.0f)
            speechService = SpeechService(rec, 16000.0f)
            speechService?.startListening(object : org.vosk.android.RecognitionListener {
                override fun onPartialResult(hypothesis: String) {
                    listener.onPartialResult(hypothesis)
                }

                override fun onResult(hypothesis: String) {
                    listener.onResult(hypothesis)
                }

                override fun onFinalResult(hypothesis: String) {
                    listener.onResult(hypothesis)
                }

                override fun onError(exception: Exception) {
                    listener.onError(exception)
                }

                override fun onTimeout() {
                    // Handle timeout
                }
            })
        } catch (e: IOException) {
            listener.onError(e)
        }
    }

    fun stopListening() {
        speechService?.stop()
        speechService = null
    }
}

package com.asha.worker.ai.text

import java.text.Normalizer

/**
 * Cleans up raw Devanagari text coming out of the Vosk speech recognizer so the
 * downstream parser can rely on a canonical form.
 *
 * Responsibilities:
 *  - Unicode NFC normalization (compose matras consistently)
 *  - fold Devanagari digits (०-९) to ASCII digits (0-9)
 *  - drop zero-width joiners / non-joiners and the nukta-less/anusvara noise
 *  - collapse runs of whitespace
 *
 * It deliberately does NOT translate words — meaning-level mapping lives in the parser.
 */
object DevanagariNormalizer {

    private const val DEVANAGARI_ZERO = '०' // ०
    private const val ZWNJ = '‌'
    private const val ZWJ = '‍'

    fun normalize(input: String): String {
        if (input.isEmpty()) return ""
        // 1) NFC compose
        var s = Normalizer.normalize(input, Normalizer.Form.NFC)
        // 2) fold Devanagari digits -> ASCII
        val sb = StringBuilder(s.length)
        for (ch in s) {
            when {
                ch in DEVANAGARI_ZERO..'९' -> sb.append(('0' + (ch - DEVANAGARI_ZERO)))
                ch == ZWNJ || ch == ZWJ -> { /* drop */ }
                else -> sb.append(ch)
            }
        }
        s = sb.toString()
        // 3) collapse whitespace and trim
        return s.trim().replace(WHITESPACE, " ")
    }

    /** Devanagari digit string (e.g. "१२") -> Int, or null. Accepts ASCII digits too. */
    fun digitsToInt(token: String): Int? = normalize(token).toIntOrNull()

    private val WHITESPACE = Regex("\\s+")
}

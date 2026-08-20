package com.asha.worker.ai.text

/**
 * Produces a fuzzy match key for a Devanagari name so that spelling/matra variations
 * of the same spoken name collapse together (e.g. राजू / राजु / राजो).
 *
 * Strategy: normalize (NFC keeps nukta as a combining mark), drop spaces and every
 * vowel sign / anusvara / visarga / virama / nukta, leaving a consonant skeleton.
 * Good enough for a village-sized patient set; refined further as needed.
 */
object PhoneticKey {

    // Vowel signs, anusvara/chandrabindu/visarga, virama (halant), nukta.
    private const val MARKS = "ािीुूृॄॅॆेैॉॊोौ्ंःँ़"

    fun of(name: String): String {
        val n = DevanagariNormalizer.normalize(name)
        val sb = StringBuilder(n.length)
        for (ch in n) {
            when {
                ch == ' ' -> {}
                MARKS.indexOf(ch) >= 0 -> {}
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }
}

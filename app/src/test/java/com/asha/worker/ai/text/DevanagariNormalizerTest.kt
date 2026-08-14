package com.asha.worker.ai.text

import org.junit.Assert.assertEquals
import org.junit.Test

class DevanagariNormalizerTest {

    @Test
    fun foldsDevanagariDigits() {
        assertEquals("123", DevanagariNormalizer.normalize("१२३"))
    }

    @Test
    fun collapsesWhitespaceAndTrims() {
        assertEquals("बुखार तीन", DevanagariNormalizer.normalize("  बुखार    तीन  "))
    }

    @Test
    fun mixedDigitsInSentence() {
        assertEquals("2 साल 5 दिन", DevanagariNormalizer.normalize("२ साल ५ दिन"))
    }

    @Test
    fun digitsToIntAcceptsDevanagari() {
        assertEquals(10, DevanagariNormalizer.digitsToInt("१०"))
    }
}

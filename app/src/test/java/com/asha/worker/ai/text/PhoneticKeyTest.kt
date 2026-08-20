package com.asha.worker.ai.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PhoneticKeyTest {

    @Test fun matraVariantsCollapse() {
        // राजू / राजु / राजो share a consonant skeleton
        assertEquals(PhoneticKey.of("राजू"), PhoneticKey.of("राजु"))
        assertEquals(PhoneticKey.of("राजू"), PhoneticKey.of("राजो"))
    }

    @Test fun longVowelNameCollapses() {
        assertEquals(PhoneticKey.of("सुनीता"), PhoneticKey.of("सुनिता"))
    }

    @Test fun differentNamesDiffer() {
        assertNotEquals(PhoneticKey.of("राजू"), PhoneticKey.of("मीना"))
    }

    @Test fun spacesIgnored() {
        assertEquals(PhoneticKey.of("रामकुमार"), PhoneticKey.of("राम कुमार"))
    }
}

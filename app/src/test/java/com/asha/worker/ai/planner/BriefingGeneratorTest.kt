package com.asha.worker.ai.planner

import org.junit.Assert.assertTrue
import org.junit.Test

class BriefingGeneratorTest {

    @Test fun emptyPlanSaysNothingDue() {
        assertTrue(BriefingGenerator.briefing(emptyList()).contains("कोई ज़रूरी विज़िट नहीं"))
    }

    @Test fun listsNumberedItems() {
        val items = listOf(
            PlanItem("मीना", "टीकाकरण", 0),
            PlanItem("राजू", "फ़ॉलो-अप", 0)
        )
        val b = BriefingGenerator.briefing(items)
        assertTrue(b.contains("मीना"))
        assertTrue(b.contains("राजू"))
        assertTrue(b.contains("1."))
        assertTrue(b.contains("2."))
    }

    @Test fun truncatesBeyondFive() {
        val items = (1..7).map { PlanItem("प$it", "फ़ॉलो-अप", 0) }
        assertTrue(BriefingGenerator.briefing(items).contains("2 अन्य"))
    }
}

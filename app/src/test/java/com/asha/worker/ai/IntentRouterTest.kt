package com.asha.worker.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class IntentRouterTest {

    private val router = IntentRouter()

    @Test fun recordVisit() =
        assertEquals(Intent.RECORD_VISIT, router.classify("सुनीता का बेटा तीन साल बुखार तीन दिन से"))

    @Test fun recallHousehold() =
        assertEquals(Intent.RECALL, router.classify("राजू का घर"))

    @Test fun bareNameIsRecall() =
        assertEquals(Intent.RECALL, router.classify("राजू"))

    @Test fun guidanceQuestion() =
        assertEquals(Intent.GUIDANCE_QUERY, router.classify("तीन दिन से बुखार हो तो क्या करें"))

    @Test fun dailyPlan() =
        assertEquals(Intent.DAILY_PLAN, router.classify("आज क्या करना है"))

    @Test fun planKeyword() =
        assertEquals(Intent.DAILY_PLAN, router.classify("आज की योजना बताओ"))
}

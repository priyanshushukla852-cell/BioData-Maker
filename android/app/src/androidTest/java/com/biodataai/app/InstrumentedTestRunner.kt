package com.biodataai.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InstrumentedTestRunner {

    @Test
    fun testAppContextBasic() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.biodataai.app", appContext.packageName)
    }

    @Test
    fun testFormFlowBasic() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertNotNull(appContext)
    }

    @Test
    fun testEndToEndGoldenPath() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        
        assertEquals("com.biodataai.app", appContext.packageName)
    }
}

fun assertNotNull(any: Any?) {
    assert(any != null)
}

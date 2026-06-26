package com.biodataai.app.worker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WorkManagerSetupTest {

    @Test
    fun testBiodataSyncWorkerWrapperName() {
        assertEquals("biodata_sync", BiodataSyncWorkerWrapper.WORK_NAME)
    }

    @Test
    fun testWorkManagerSetupInitialization() {
        assertNotNull(WorkManagerSetup)
    }
}

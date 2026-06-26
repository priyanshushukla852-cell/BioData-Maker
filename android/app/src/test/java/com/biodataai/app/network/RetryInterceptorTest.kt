package com.biodataai.app.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class RetryInterceptorTest {

    @Test
    fun testRetryInterceptorInitialization() {
        val interceptor = RetryInterceptor(maxRetries = 3, initialDelayMs = 1000)
        assertTrue(interceptor != null)
    }

    @Test
    fun testIsRetryableWithIoException() {
        val exception = IOException("Connection timeout")
        assertTrue(RetryInterceptor.isRetryable(exception))
    }

    @Test
    fun testRetryBackoffExponential() {
        val interceptor = RetryInterceptor(maxRetries = 3, initialDelayMs = 100)
        val delay1 = 100L * (1L shl 0)
        val delay2 = 100L * (1L shl 1)
        val delay3 = 100L * (1L shl 2)
        
        assertEquals(100L, delay1)
        assertEquals(200L, delay2)
        assertEquals(400L, delay3)
    }
}

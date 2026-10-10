package com.example.taskerplugin

import org.junit.Assert.assertNotNull
import org.junit.Test

class EventReceiverTest {

    @Test
    fun testEventReceiverInstantiation() {
        val receiver = EventReceiver()
        assertNotNull(receiver)
    }
}

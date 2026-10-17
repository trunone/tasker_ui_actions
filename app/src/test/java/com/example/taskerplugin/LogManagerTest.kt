package com.example.taskerplugin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LogManagerTest {

    @Before
    fun setUp() {
        LogManager.clearLogs()
        LogManager.clearTaskerViewIds()
        LogManager.isLoggingEnabled = true
    }

    @Test
    fun testLoggingDisabledByDefaultAndToggle() {
        LogManager.isLoggingEnabled = false
        assertFalse(LogManager.isLoggingEnabled)

        LogManager.i("TestTag", "Should not be logged")
        assertEquals(0, LogManager.getLogs().size)

        LogManager.isLoggingEnabled = true
        assertTrue(LogManager.isLoggingEnabled)

        LogManager.i("TestTag", "Should be logged")
        assertEquals(1, LogManager.getLogs().size)
        assertEquals("Should be logged", LogManager.getLogs()[0].message)
    }

    @Test
    fun testLogLevelsAndGetLogs() {
        LogManager.v("TestTag", "Verbose message")
        LogManager.d("TestTag", "Debug message")
        LogManager.i("TestTag", "Info message")
        LogManager.w("TestTag", "Warn message")
        LogManager.e("TestTag", "Error message")

        val allLogs = LogManager.getLogs()
        assertEquals(5, allLogs.size)

        val debugLogs = LogManager.getLogs(LogLevel.DEBUG)
        assertEquals(4, debugLogs.size)

        val infoLogs = LogManager.getLogs(LogLevel.INFO)
        assertEquals(3, infoLogs.size)

        val warnLogs = LogManager.getLogs(LogLevel.WARN)
        assertEquals(2, warnLogs.size)

        val errorLogs = LogManager.getLogs(LogLevel.ERROR)
        assertEquals(1, errorLogs.size)
        assertEquals("Error message", errorLogs[0].message)
    }

    @Test
    fun testTaskerViewIdTrackingAndLogLevels() {
        val taskerId = "com.app:id/tasker_button"
        val nonTaskerId = "com.app:id/other_button"

        assertFalse(LogManager.isTaskerViewId(taskerId))
        LogManager.registerTaskerViewId(taskerId)
        assertTrue(LogManager.isTaskerViewId(taskerId))
        assertFalse(LogManager.isTaskerViewId(nonTaskerId))

        // Simulate detection logic
        fun logDetection(id: String) {
            if (LogManager.isTaskerViewId(id)) {
                LogManager.i("ClickService", "ID detected: $id")
            } else {
                LogManager.d("ClickService", "ID detected: $id")
            }
        }

        logDetection(taskerId)
        logDetection(nonTaskerId)

        val logs = LogManager.getLogs()
        val taskerLog = logs.find { it.message == "ID detected: $taskerId" }
        val nonTaskerLog = logs.find { it.message == "ID detected: $nonTaskerId" }

        assertNotNull(taskerLog)
        assertNotNull(nonTaskerLog)
        assertEquals(LogLevel.INFO, taskerLog?.level)
        assertEquals(LogLevel.DEBUG, nonTaskerLog?.level)
    }

    @Test
    fun testClearLogs() {
        LogManager.i("TestTag", "Sample log")
        assertEquals(1, LogManager.getLogs().size)

        LogManager.clearLogs()
        assertEquals(0, LogManager.getLogs().size)
    }

    @Test
    fun testLogListenerCallbacks() {
        val addedLogs = mutableListOf<LogEntry>()
        var clearedCalled = false

        val listener = object : LogListener {
            override fun onLogAdded(entry: LogEntry) {
                addedLogs.add(entry)
            }

            override fun onLogsCleared() {
                clearedCalled = true
            }
        }

        LogManager.addListener(listener)
        LogManager.i("ClickService", "ID detected: com.example.app:id/button1")
        LogManager.i("ClickService", "ID clicked: com.example.app:id/button1")

        assertEquals(2, addedLogs.size)
        assertEquals("ID detected: com.example.app:id/button1", addedLogs[0].message)
        assertEquals("ID clicked: com.example.app:id/button1", addedLogs[1].message)

        LogManager.clearLogs()
        assertTrue(clearedCalled)

        LogManager.removeListener(listener)
        LogManager.d("TestTag", "After removal")
        assertEquals(2, addedLogs.size) // Listener should not receive new logs
    }

    @Test
    fun testIdDetectedAndClickedLogs() {
        LogManager.i("ClickService", "ID detected: com.example.app:id/test_button")
        LogManager.i("ClickService", "ID clicked: com.example.app:id/test_button")

        val infoLogs = LogManager.getLogs(LogLevel.INFO)
        val detectedLog = infoLogs.find { it.message.contains("ID detected:") }
        val clickedLog = infoLogs.find { it.message.contains("ID clicked:") }

        assertNotNull(detectedLog)
        assertNotNull(clickedLog)
        assertEquals(LogLevel.INFO, detectedLog?.level)
        assertEquals(LogLevel.INFO, clickedLog?.level)
    }

    @Test
    fun testLogEntryFormattedTime() {
        val entry = LogEntry(
            id = 1,
            timestamp = System.currentTimeMillis(),
            level = LogLevel.INFO,
            tag = "TestTag",
            message = "Test message"
        )
        assertNotNull(entry.formattedTime())
        assertTrue(entry.formattedTime().isNotEmpty())
    }
}

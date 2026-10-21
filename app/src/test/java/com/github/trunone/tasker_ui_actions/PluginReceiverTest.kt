package com.github.trunone.tasker_ui_actions

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class PluginReceiverTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        ClickService.instance = null
        LogManager.clearLogs()
        LogManager.isLoggingEnabled = true
    }

    private fun sendOrderedSettingIntent(intent: Intent): Triple<Int, String?, Bundle?> {
        var finalCode = 0
        var finalData: String? = null
        var finalExtras: Bundle? = null

        val resultReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                finalCode = resultCode
                finalData = resultData
                finalExtras = getResultExtras(false)
            }
        }

        context.sendOrderedBroadcast(
            intent,
            null,
            resultReceiver,
            null,
            Activity.RESULT_OK,
            null,
            null
        )

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        return Triple(finalCode, finalData, finalExtras)
    }

    @Test
    fun testOnReceiveOtherActionDoesNothing() {
        val intent = Intent("com.example.OTHER_ACTION")
        context.sendOrderedBroadcast(intent, null)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertEquals(0, LogManager.getLogs().size)
    }

    @Test
    fun testOnReceiveMissingBundle() {
        val intent = Intent(Constants.ACTION_FIRE_SETTING)
        val (code, data, extras) = sendOrderedSettingIntent(intent)

        assertEquals(Activity.RESULT_CANCELED, code)
        assertEquals("Action failed: Missing extra bundle.", data)
        assertNotNull(extras)
        assertEquals("Action failed: Missing extra bundle.", extras?.getString("%errmsg"))
        assertEquals("Action failed: Missing extra bundle.", extras?.getString("net.dinglisch.android.tasker.extras.VARIABLE_STRING_RESULT"))
    }

    @Test
    fun testOnReceiveMissingViewId() {
        val intent = Intent(Constants.ACTION_FIRE_SETTING).apply {
            putExtra(Constants.EXTRA_BUNDLE, Bundle())
        }
        val (code, data, extras) = sendOrderedSettingIntent(intent)

        assertEquals(Activity.RESULT_CANCELED, code)
        assertEquals("Action failed: View ID is missing or empty.", data)
        assertNotNull(extras)
        assertEquals("Action failed: View ID is missing or empty.", extras?.getString("%errmsg"))
    }

    @Test
    fun testOnReceiveServiceNotRunning() {
        val bundle = Bundle().apply {
            putString(Constants.BUNDLE_EXTRA_VIEW_ID, "com.example.app:id/button")
        }
        val intent = Intent(Constants.ACTION_FIRE_SETTING).apply {
            putExtra(Constants.EXTRA_BUNDLE, bundle)
        }
        val (code, data, extras) = sendOrderedSettingIntent(intent)

        assertEquals(Activity.RESULT_CANCELED, code)
        assertEquals("Action failed: Accessibility Service not running.", data)
        assertNotNull(extras)
        assertEquals("Action failed: Accessibility Service not running.", extras?.getString("%errmsg"))
    }

    @Test
    fun testOnReceiveClickFailed() {
        val testService = TestClickService(clickResult = false)
        ClickService.instance = testService

        val viewId = "com.example.app:id/button"
        val bundle = Bundle().apply {
            putString(Constants.BUNDLE_EXTRA_VIEW_ID, viewId)
        }
        val intent = Intent(Constants.ACTION_FIRE_SETTING).apply {
            putExtra(Constants.EXTRA_BUNDLE, bundle)
        }
        val (code, data, extras) = sendOrderedSettingIntent(intent)

        assertEquals(Activity.RESULT_CANCELED, code)
        assertEquals("Click failed: View ID 'com.example.app:id/button' not found or not clickable.", data)
        assertNotNull(extras)
        assertEquals("Click failed: View ID 'com.example.app:id/button' not found or not clickable.", extras?.getString("%errmsg"))
    }

    @Test
    fun testOnReceiveClickSucceeded() {
        val testService = TestClickService(clickResult = true)
        ClickService.instance = testService

        val viewId = "com.example.app:id/button"
        val bundle = Bundle().apply {
            putString(Constants.BUNDLE_EXTRA_VIEW_ID, viewId)
        }
        val intent = Intent(Constants.ACTION_FIRE_SETTING).apply {
            putExtra(Constants.EXTRA_BUNDLE, bundle)
        }
        val (code, _, _) = sendOrderedSettingIntent(intent)

        assertEquals(Activity.RESULT_OK, code)
    }

    private class TestClickService(private val clickResult: Boolean) : ClickService() {
        override fun performClick(viewId: String): Boolean {
            return clickResult
        }
    }
}

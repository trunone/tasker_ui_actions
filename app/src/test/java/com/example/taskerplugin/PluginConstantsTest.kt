package com.example.taskerplugin

import org.junit.Assert.assertEquals
import org.junit.Test

class PluginConstantsTest {

    @Test
    fun testConstantsValues() {
        assertEquals("com.twofortyfouram.locale.intent.action.FIRE_SETTING", Constants.ACTION_FIRE_SETTING)
        assertEquals("com.twofortyfouram.locale.intent.action.EDIT_CONDITION", Constants.ACTION_EDIT_CONDITION)
        assertEquals("com.twofortyfouram.locale.intent.action.QUERY_CONDITION", Constants.ACTION_QUERY_CONDITION)
        assertEquals("com.twofortyfouram.locale.intent.action.REQUEST_QUERY", Constants.ACTION_REQUEST_QUERY)
        assertEquals("com.twofortyfouram.locale.intent.extra.BUNDLE", Constants.EXTRA_BUNDLE)
        assertEquals("com.twofortyfouram.locale.intent.extra.BLURB", Constants.EXTRA_STRING_BLURB)
        assertEquals("com.twofortyfouram.locale.intent.extra.ACTIVITY", Constants.EXTRA_ACTIVITY)
        assertEquals(16, Constants.RESULT_CONDITION_SATISFIED)
        assertEquals(17, Constants.RESULT_CONDITION_UNSATISFIED)
        assertEquals(18, Constants.RESULT_CONDITION_UNKNOWN)
        assertEquals("com.example.taskerplugin.extra.VIEW_ID", Constants.BUNDLE_EXTRA_VIEW_ID)
        assertEquals("com.example.taskerplugin.extra.TARGET_ACTIVITY", Constants.EXTRA_TARGET_ACTIVITY)
    }

    @Test
    fun testBlurbFormat() {
        val viewId = "com.example.app:id/button"
        val blurb = if (viewId.isNotEmpty()) "Click: $viewId" else "Click: (not configured)"
        assertEquals("Click: com.example.app:id/button", blurb)

        val emptyViewId = ""
        val emptyBlurb = if (emptyViewId.isNotEmpty()) "Click: $emptyViewId" else "Click: (not configured)"
        assertEquals("Click: (not configured)", emptyBlurb)
    }

    @Test
    fun testStateBlurbFormat() {
        val viewId = "com.example.app:id/status_icon"
        val blurb = if (viewId.isNotEmpty()) "View Visible: $viewId" else "View Visible: (not configured)"
        assertEquals("View Visible: com.example.app:id/status_icon", blurb)

        val emptyViewId = ""
        val emptyBlurb = if (emptyViewId.isNotEmpty()) "View Visible: $emptyViewId" else "View Visible: (not configured)"
        assertEquals("View Visible: (not configured)", emptyBlurb)
    }
}

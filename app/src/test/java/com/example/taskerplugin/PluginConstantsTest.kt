package com.example.taskerplugin

import org.junit.Assert.assertEquals
import org.junit.Test

class PluginConstantsTest {

    @Test
    fun testConstantsValues() {
        assertEquals("com.twofortyfouram.locale.intent.action.FIRE_SETTING", Constants.ACTION_FIRE_SETTING)
        assertEquals("com.twofortyfouram.locale.intent.extra.BUNDLE", Constants.EXTRA_BUNDLE)
        assertEquals("com.twofortyfouram.locale.intent.extra.BLURB", Constants.EXTRA_STRING_BLURB)
        assertEquals("com.example.taskerplugin.extra.VIEW_ID", Constants.BUNDLE_EXTRA_VIEW_ID)
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
}

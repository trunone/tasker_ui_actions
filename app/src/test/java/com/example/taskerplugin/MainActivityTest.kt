package com.example.taskerplugin

import android.widget.TextView
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivityTest {

    @Test
    fun testVersionTextDisplayed() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val textVersion = activity.findViewById<TextView>(R.id.text_version)
        assertEquals("Version: ${BuildConfig.VERSION_NAME}", textVersion.text.toString())
    }
}

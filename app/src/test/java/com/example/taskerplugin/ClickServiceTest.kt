package com.example.taskerplugin

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ClickServiceTest {

    @Before
    fun setUp() {
        ClickService.clearRecentViewIds()
    }

    @Test
    fun testAddAndGetRecentViewIds() {
        ClickService.addRecentViewId("com.app:id/button1")
        ClickService.addRecentViewId("com.app:id/button2")

        val recent = ClickService.getRecentViewIds()
        assertEquals(2, recent.size)
        // Newest first
        assertEquals("com.app:id/button2", recent[0])
        assertEquals("com.app:id/button1", recent[1])
    }

    @Test
    fun testDeduplicationAndReordering() {
        ClickService.addRecentViewId("com.app:id/button1")
        ClickService.addRecentViewId("com.app:id/button2")
        ClickService.addRecentViewId("com.app:id/button1")

        val recent = ClickService.getRecentViewIds()
        assertEquals(2, recent.size)
        // button1 is newest now
        assertEquals("com.app:id/button1", recent[0])
        assertEquals("com.app:id/button2", recent[1])
    }

    @Test
    fun testMaxCapacity() {
        for (i in 1..25) {
            ClickService.addRecentViewId("com.app:id/button$i")
        }

        val recent = ClickService.getRecentViewIds()
        assertEquals(20, recent.size)
        // Newest should be button25, oldest remaining should be button6
        assertEquals("com.app:id/button25", recent[0])
        assertEquals("com.app:id/button6", recent[19])
    }

    @Test
    fun testClearRecentViewIds() {
        ClickService.addRecentViewId("com.app:id/button1")
        ClickService.clearRecentViewIds()

        val recent = ClickService.getRecentViewIds()
        assertEquals(0, recent.size)
    }

    @Test
    fun testTraverseNullNode() {
        val (clickable, all, pkgName) = ClickService.traverseNodesForViewIds(null)
        assertEquals(0, clickable.size)
        assertEquals(0, all.size)
        assertEquals(null, pkgName)
    }

    @Test
    fun testTraverseWithIgnoredPackages() {
        val (clickable, all, pkgName) = ClickService.traverseNodesForViewIds(
            null,
            ignoredPackages = setOf("com.example.taskerplugin", "com.android.systemui")
        )
        assertEquals(0, clickable.size)
        assertEquals(0, all.size)
        assertEquals(null, pkgName)
    }
}

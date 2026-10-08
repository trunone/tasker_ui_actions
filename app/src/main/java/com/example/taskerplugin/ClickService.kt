package com.example.taskerplugin

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ClickService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Ignore events from our own app
        val eventPackage = event.packageName?.toString()
        if (eventPackage != null && eventPackage == packageName) {
            return
        }

        val source = event.source ?: return
        val viewId = extractViewId(source)
        if (!viewId.isNullOrEmpty()) {
            addRecentViewId(viewId)
        }
    }

    private fun extractViewId(node: AccessibilityNodeInfo): String? {
        if (!node.viewIdResourceName.isNullOrEmpty()) {
            return node.viewIdResourceName
        }
        var current: AccessibilityNodeInfo? = node.parent
        while (current != null) {
            if (!current.viewIdResourceName.isNullOrEmpty()) {
                return current.viewIdResourceName
            }
            current = current.parent
        }
        return null
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d("ClickService", "Service connected")

        val info = serviceInfo
        info.flags = info.flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        serviceInfo = info
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    fun performClick(viewId: String): Boolean {
        Log.d("ClickService", "Attempting to click view with ID: $viewId")
        val rootNode = rootInActiveWindow
        if (rootNode == null) {
            Log.e("ClickService", "Root node is null")
            return false
        }

        var clicked = false
        val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)

        if (nodes != null) {
            for (node in nodes) {
                if (!clicked) {
                    clicked = tryClick(node)
                }
            }
        } else {
            Log.d("ClickService", "No nodes found for ID: $viewId")
        }

        return clicked
    }

    private fun tryClick(node: AccessibilityNodeInfo): Boolean {
        if (node.isClickable) {
            Log.d("ClickService", "Clicking node: ${node.viewIdResourceName}")
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }

        var current = node.parent
        while (current != null) {
            if (current.isClickable) {
                Log.d("ClickService", "Clicking parent node: ${current.viewIdResourceName}")
                return current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            current = current.parent
        }
        return false
    }

    companion object {
        var instance: ClickService? = null
        private const val MAX_RECENT_VIEW_IDS = 20
        private val recentViewIds = LinkedHashSet<String>()

        fun addRecentViewId(viewId: String) {
            synchronized(recentViewIds) {
                recentViewIds.remove(viewId)
                recentViewIds.add(viewId)
                if (recentViewIds.size > MAX_RECENT_VIEW_IDS) {
                    val iterator = recentViewIds.iterator()
                    if (iterator.hasNext()) {
                        iterator.next()
                        iterator.remove()
                    }
                }
            }
        }

        fun getRecentViewIds(): List<String> {
            synchronized(recentViewIds) {
                return recentViewIds.toList().reversed()
            }
        }

        fun clearRecentViewIds() {
            synchronized(recentViewIds) {
                recentViewIds.clear()
            }
        }
    }
}

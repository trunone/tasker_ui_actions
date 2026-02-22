package com.example.taskerplugin

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ClickService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op
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
                node.recycle()
            }
        } else {
            Log.d("ClickService", "No nodes found for ID: $viewId")
        }

        rootNode.recycle()
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
                 val result = current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                 current.recycle()
                 return result
             }
             val next = current.parent
             current.recycle()
             current = next
        }
        return false
    }

    companion object {
        var instance: ClickService? = null
    }
}

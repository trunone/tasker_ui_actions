package com.example.taskerplugin

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.core.app.NotificationCompat

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

    fun showCaptureNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val captureIntent = Intent(this, CaptureReceiver::class.java).apply {
            action = CaptureReceiver.ACTION_CAPTURE_VIEW_IDS
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            captureIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        Toast.makeText(this, getString(R.string.notification_posted_toast), Toast.LENGTH_LONG).show()
    }

    fun captureCurrentWindowViewIds() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)

        val rootNode = rootInActiveWindow
        val (clickableIds, allIds, targetPackage) = traverseNodesForViewIds(rootNode)

        val viewIdsToReturn = if (clickableIds.isNotEmpty()) clickableIds else allIds

        val intent = Intent(this, PluginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putStringArrayListExtra(Constants.EXTRA_CAPTURED_VIEW_IDS, ArrayList(viewIdsToReturn))
            putExtra(Constants.EXTRA_CAPTURED_PACKAGE_NAME, targetPackage)
        }
        startActivity(intent)
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
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "view_id_capture_channel"
        private const val MAX_RECENT_VIEW_IDS = 20
        private val recentViewIds = LinkedHashSet<String>()

        fun traverseNodesForViewIds(root: AccessibilityNodeInfo?): Triple<List<String>, List<String>, String?> {
            if (root == null) return Triple(emptyList(), emptyList(), null)

            val clickableIds = LinkedHashSet<String>()
            val allIds = LinkedHashSet<String>()
            var packageNameStr: String? = root.packageName?.toString()

            val queue = ArrayDeque<AccessibilityNodeInfo>()
            queue.add(root)

            while (queue.isNotEmpty()) {
                val node = queue.removeFirst()
                if (packageNameStr == null && node.packageName != null) {
                    packageNameStr = node.packageName.toString()
                }

                val viewId = node.viewIdResourceName
                if (!viewId.isNullOrEmpty()) {
                    allIds.add(viewId)
                    if (isNodeOrParentClickable(node)) {
                        clickableIds.add(viewId)
                    }
                }

                for (i in 0 until node.childCount) {
                    val child = node.getChild(i)
                    if (child != null) {
                        queue.add(child)
                    }
                }
            }

            return Triple(clickableIds.toList(), allIds.toList(), packageNameStr)
        }

        private fun isNodeOrParentClickable(node: AccessibilityNodeInfo): Boolean {
            if (node.isClickable) return true
            var parent = node.parent
            while (parent != null) {
                if (parent.isClickable) return true
                parent = parent.parent
            }
            return false
        }

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

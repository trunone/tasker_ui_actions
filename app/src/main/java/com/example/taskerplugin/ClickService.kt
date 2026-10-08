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
import android.view.accessibility.AccessibilityWindowInfo
import android.widget.Toast
import androidx.core.app.NotificationCompat

class ClickService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val eventPackage = event.packageName?.toString()
        if (eventPackage != null && eventPackage != packageName && eventPackage != SYSTEM_UI_PACKAGE) {
            lastActiveAppPackage = eventPackage
        }

        // Ignore events from our own app
        if (eventPackage != null && eventPackage == packageName) {
            return
        }

        val source = event.source ?: return
        val (clickableIds, allIds, _) = traverseNodesForViewIds(
            source,
            ignoredPackages = setOf(packageName, SYSTEM_UI_PACKAGE)
        )
        val extractedIds = if (clickableIds.isNotEmpty()) clickableIds else allIds
        for (id in extractedIds) {
            addRecentViewId(id)
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

    fun showCaptureNotification(activityPendingIntent: PendingIntent? = null) {
        if (activityPendingIntent != null) {
            capturedActivityPendingIntent = activityPendingIntent
        }

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

        val (rootNode, targetPkg) = getTargetApplicationRootNode()
        val (clickableIds, allIds, extractedPkg) = traverseNodesForViewIds(
            rootNode,
            ignoredPackages = setOf(packageName, SYSTEM_UI_PACKAGE)
        )

        val finalPkg = targetPkg ?: extractedPkg
        var viewIdsToReturn = if (clickableIds.isNotEmpty()) clickableIds else allIds
        if (viewIdsToReturn.isEmpty()) {
            val recents = getRecentViewIds()
            if (recents.isNotEmpty()) {
                viewIdsToReturn = recents
            }
        }

        val fillInIntent = Intent().apply {
            putStringArrayListExtra(Constants.EXTRA_CAPTURED_VIEW_IDS, ArrayList(viewIdsToReturn))
            putExtra(Constants.EXTRA_CAPTURED_PACKAGE_NAME, finalPkg)
        }

        val pendingIntent = capturedActivityPendingIntent
        capturedActivityPendingIntent = null

        if (pendingIntent != null) {
            try {
                pendingIntent.send(this, 0, fillInIntent)
                return
            } catch (e: Exception) {
                Log.e("ClickService", "Failed to send pending intent", e)
            }
        }

        val intent = Intent(this, PluginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putStringArrayListExtra(Constants.EXTRA_CAPTURED_VIEW_IDS, ArrayList(viewIdsToReturn))
            putExtra(Constants.EXTRA_CAPTURED_PACKAGE_NAME, finalPkg)
        }
        startActivity(intent)
    }

    private fun getTargetApplicationRootNode(): Pair<AccessibilityNodeInfo?, String?> {
        val lastPkg = lastActiveAppPackage

        try {
            val windowList = windows
            if (!windowList.isNullOrEmpty()) {
                if (lastPkg != null) {
                    for (window in windowList) {
                        val root = window.root
                        if (root != null && root.packageName?.toString() == lastPkg) {
                            return Pair(root, lastPkg)
                        }
                    }
                }

                for (window in windowList) {
                    if (window.type == AccessibilityWindowInfo.TYPE_APPLICATION) {
                        val root = window.root
                        val pkg = root?.packageName?.toString()
                        if (pkg != null && pkg != packageName && pkg != SYSTEM_UI_PACKAGE) {
                            return Pair(root, pkg)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ClickService", "Error accessing windows list", e)
        }

        val activeRoot = rootInActiveWindow
        val activePkg = activeRoot?.packageName?.toString()
        if (activePkg != null && activePkg != packageName && activePkg != SYSTEM_UI_PACKAGE) {
            return Pair(activeRoot, activePkg)
        }

        return Pair(null, lastPkg)
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
        const val SYSTEM_UI_PACKAGE = "com.android.systemui"
        private const val MAX_RECENT_VIEW_IDS = 20
        private val recentViewIds = LinkedHashSet<String>()
        var lastActiveAppPackage: String? = null
        private var capturedActivityPendingIntent: PendingIntent? = null

        fun traverseNodesForViewIds(
            root: AccessibilityNodeInfo?,
            ignoredPackages: Set<String> = emptySet()
        ): Triple<List<String>, List<String>, String?> {
            if (root == null) return Triple(emptyList(), emptyList(), null)

            val clickableIds = LinkedHashSet<String>()
            val allIds = LinkedHashSet<String>()
            var packageNameStr: String? = root.packageName?.toString()

            val queue = ArrayDeque<AccessibilityNodeInfo>()
            queue.add(root)

            while (queue.isNotEmpty()) {
                val node = queue.removeFirst()
                val nodePkg = node.packageName?.toString()
                if (packageNameStr == null && nodePkg != null && !ignoredPackages.contains(nodePkg)) {
                    packageNameStr = nodePkg
                }

                if (nodePkg == null || !ignoredPackages.contains(nodePkg)) {
                    val viewId = node.viewIdResourceName
                    if (!viewId.isNullOrEmpty()) {
                        val isIgnored = ignoredPackages.any { pkg -> viewId.startsWith("$pkg:") }
                        if (!isIgnored) {
                            allIds.add(viewId)
                            if (isNodeOrParentClickable(node)) {
                                clickableIds.add(viewId)
                            }
                        }
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

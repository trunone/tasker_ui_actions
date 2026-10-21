package com.github.trunone.tasker_ui_actions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CaptureReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ACTION_CAPTURE_VIEW_IDS) {
            val targetActivity = intent.getStringExtra(Constants.EXTRA_TARGET_ACTIVITY)
            ClickService.instance?.captureCurrentWindowViewIds(targetActivity)
        }
    }

    companion object {
        const val ACTION_CAPTURE_VIEW_IDS = "com.github.trunone.tasker_ui_actions.ACTION_CAPTURE_VIEW_IDS"
    }
}

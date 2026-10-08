package com.example.taskerplugin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CaptureReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ACTION_CAPTURE_VIEW_IDS) {
            ClickService.instance?.captureCurrentWindowViewIds()
        }
    }

    companion object {
        const val ACTION_CAPTURE_VIEW_IDS = "com.example.taskerplugin.ACTION_CAPTURE_VIEW_IDS"
    }
}

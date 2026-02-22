package com.example.taskerplugin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PluginReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Constants.ACTION_FIRE_SETTING) {
            val bundle = intent.getBundleExtra(Constants.EXTRA_BUNDLE)
            if (bundle != null) {
                val viewId = bundle.getString(Constants.BUNDLE_EXTRA_VIEW_ID)
                if (viewId != null) {
                    val service = ClickService.instance
                    if (service != null) {
                        Log.d("PluginReceiver", "Requesting click for $viewId")
                        val success = service.performClick(viewId)
                        if (success) {
                            Log.d("PluginReceiver", "Click successful")
                        } else {
                            Log.w("PluginReceiver", "Click failed or element not found")
                        }
                    } else {
                        Log.e("PluginReceiver", "Accessibility Service not running. Please enable it in Settings.")
                    }
                }
            }
        }
    }
}

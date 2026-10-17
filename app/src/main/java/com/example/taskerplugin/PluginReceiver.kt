package com.example.taskerplugin

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle

class PluginReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Constants.ACTION_FIRE_SETTING) {
            val bundle = intent.getBundleExtra(Constants.EXTRA_BUNDLE)
            if (bundle == null) {
                returnError("Action failed: Missing extra bundle.")
                return
            }

            val viewId = bundle.getString(Constants.BUNDLE_EXTRA_VIEW_ID)
            if (viewId.isNullOrEmpty()) {
                returnError("Action failed: View ID is missing or empty.")
                return
            }

            LogManager.registerTaskerViewId(viewId)
            val service = ClickService.instance
            if (service == null) {
                returnError("Action failed: Accessibility Service not running.")
                return
            }

            LogManager.i("PluginReceiver", "Requesting click for ID: $viewId")
            val success = service.performClick(viewId)
            if (success) {
                LogManager.i("PluginReceiver", "Click action succeeded for ID: $viewId")
                if (isOrderedBroadcast) {
                    resultCode = Activity.RESULT_OK
                }
            } else {
                returnError("Click failed: View ID '$viewId' not found or not clickable.")
            }
        }
    }

    private fun returnError(errorMessage: String) {
        LogManager.e("PluginReceiver", errorMessage)
        if (isOrderedBroadcast) {
            val extras = try {
                getResultExtras(true) ?: Bundle()
            } catch (e: Exception) {
                Bundle()
            }
            extras.putString("net.dinglisch.android.tasker.extras.VARIABLE_STRING_RESULT", errorMessage)
            extras.putString("%errmsg", errorMessage)
            extras.putString("errmsg", errorMessage)
            extras.putString("error", errorMessage)
            setResult(Activity.RESULT_CANCELED, errorMessage, extras)
        }
    }
}

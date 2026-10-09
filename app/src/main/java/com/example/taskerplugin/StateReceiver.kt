package com.example.taskerplugin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class StateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Constants.ACTION_QUERY_CONDITION) {
            val bundle = intent.getBundleExtra(Constants.EXTRA_BUNDLE)
            val viewId = bundle?.getString(Constants.BUNDLE_EXTRA_VIEW_ID)

            val service = ClickService.instance
            if (service == null) {
                Log.w("StateReceiver", "ClickService not running, returning RESULT_CONDITION_UNKNOWN")
                resultCode = Constants.RESULT_CONDITION_UNKNOWN
                return
            }

            val isVisible = service.isViewIdVisible(viewId)
            Log.d("StateReceiver", "Query condition for viewId '$viewId': visible=$isVisible")

            resultCode = if (isVisible) {
                Constants.RESULT_CONDITION_SATISFIED
            } else {
                Constants.RESULT_CONDITION_UNSATISFIED
            }
        }
    }
}

package com.example.taskerplugin

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class PluginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plugin)

        val editViewId = findViewById<EditText>(R.id.edit_view_id)
        val buttonPickViewId = findViewById<Button>(R.id.button_pick_view_id)
        val buttonSave = findViewById<Button>(R.id.button_save)

        buttonPickViewId.setOnClickListener {
            if (ClickService.instance == null) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.service_not_running_title)
                    .setMessage(R.string.service_not_running_message)
                    .setPositiveButton(R.string.open_settings) { _, _ ->
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            } else {
                val recentIds = ClickService.getRecentViewIds()
                if (recentIds.isEmpty()) {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.pick_view_id_title)
                        .setMessage(R.string.no_view_ids_found)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.pick_view_id_title)
                        .setItems(recentIds.toTypedArray()) { _, which ->
                            editViewId.setText(recentIds[which])
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                }
            }
        }

        // If editing an existing action, load the value
        val intent = intent
        val bundle = intent.getBundleExtra(Constants.EXTRA_BUNDLE)
        if (bundle != null) {
            val viewId = bundle.getString(Constants.BUNDLE_EXTRA_VIEW_ID)
            if (viewId != null) {
                editViewId.setText(viewId)
            }
        }

        buttonSave.setOnClickListener {
            val viewId = editViewId.text.toString()

            val resultIntent = Intent()
            val resultBundle = Bundle()
            resultBundle.putString(Constants.BUNDLE_EXTRA_VIEW_ID, viewId)
            resultIntent.putExtra(Constants.EXTRA_BUNDLE, resultBundle)

            // Blurb is the text shown in Tasker configuration
            val blurb = if (viewId.isNotEmpty()) "Click: $viewId" else "Click: (not configured)"
            resultIntent.putExtra(Constants.EXTRA_STRING_BLURB, blurb)

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}

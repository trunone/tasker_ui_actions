package com.example.taskerplugin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class PluginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plugin)

        val editViewId = findViewById<EditText>(R.id.edit_view_id)
        val buttonSave = findViewById<Button>(R.id.button_save)

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

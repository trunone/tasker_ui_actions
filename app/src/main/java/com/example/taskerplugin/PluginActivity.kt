package com.example.taskerplugin

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PluginActivity : AppCompatActivity() {

    private lateinit var editViewId: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plugin)

        editViewId = findViewById(R.id.edit_view_id)
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        REQUEST_CODE_NOTIFICATION_PERM
                    )
                } else {
                    ClickService.instance?.showCaptureNotification()
                }
            }
        }

        // If editing an existing action, load the value
        val bundle = intent?.getBundleExtra(Constants.EXTRA_BUNDLE)
        if (bundle != null) {
            val viewId = bundle.getString(Constants.BUNDLE_EXTRA_VIEW_ID)
            if (viewId != null) {
                editViewId.setText(viewId)
            }
        }

        handleCapturedViewIdsIntent(intent)

        buttonSave.setOnClickListener {
            val viewId = editViewId.text.toString()

            val resultIntent = Intent()
            val resultBundle = Bundle()
            resultBundle.putString(Constants.BUNDLE_EXTRA_VIEW_ID, viewId)
            resultIntent.putExtra(Constants.EXTRA_BUNDLE, resultBundle)

            val blurb = if (viewId.isNotEmpty()) "Click: $viewId" else "Click: (not configured)"
            resultIntent.putExtra(Constants.EXTRA_STRING_BLURB, blurb)

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleCapturedViewIdsIntent(intent)
    }

    private fun handleCapturedViewIdsIntent(intent: Intent?) {
        if (intent == null) return
        val capturedIds = intent.getStringArrayListExtra(Constants.EXTRA_CAPTURED_VIEW_IDS) ?: return
        val packageName = intent.getStringExtra(Constants.EXTRA_CAPTURED_PACKAGE_NAME)

        intent.removeExtra(Constants.EXTRA_CAPTURED_VIEW_IDS)

        if (capturedIds.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle(R.string.pick_view_id_title)
                .setMessage(R.string.no_view_ids_in_window)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        } else {
            val title = if (!packageName.isNullOrEmpty()) {
                getString(R.string.captured_view_ids_title, packageName)
            } else {
                getString(R.string.pick_view_id_title)
            }

            AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(capturedIds.toTypedArray()) { _, which ->
                    val selectedId = capturedIds[which]
                    editViewId.setText(selectedId)
                    copyToClipboard(selectedId)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("View ID", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, getString(R.string.id_copied_to_clipboard, text), Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERM) {
            ClickService.instance?.showCaptureNotification()
        }
    }

    companion object {
        private const val REQUEST_CODE_NOTIFICATION_PERM = 101
    }
}

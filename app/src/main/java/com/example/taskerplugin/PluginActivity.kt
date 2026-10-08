package com.example.taskerplugin

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PluginActivity : AppCompatActivity() {

    private lateinit var editViewId: EditText
    private var callerPackage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plugin)

        if (savedInstanceState != null) {
            callerPackage = savedInstanceState.getString(KEY_CALLER_PACKAGE)
        }
        if (callerPackage == null) {
            callerPackage = callingPackage ?: referrer?.host
        }

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
                    triggerCaptureNotification()
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

            val targetPkg = callerPackage
            if (!targetPkg.isNullOrEmpty()) {
                val launchIntent = packageManager.getLaunchIntentForPackage(targetPkg)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                }
                if (launchIntent != null) {
                    try {
                        startActivity(launchIntent)
                    } catch (e: Exception) {
                        // Ignore if launch intent fails
                    }
                }
            }

            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_CALLER_PACKAGE, callerPackage)
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
                    editViewId.setText(capturedIds[which])
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun triggerCaptureNotification() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, PluginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        ClickService.instance?.showCaptureNotification(pendingIntent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERM) {
            triggerCaptureNotification()
        }
    }

    companion object {
        private const val REQUEST_CODE_NOTIFICATION_PERM = 101
        private const val KEY_CALLER_PACKAGE = "key_caller_package"
    }
}

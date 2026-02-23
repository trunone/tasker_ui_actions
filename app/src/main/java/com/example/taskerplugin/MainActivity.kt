package com.example.taskerplugin

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var textStatus: TextView
    private lateinit var buttonEnableService: Button
    private lateinit var textHelp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textStatus = findViewById(R.id.text_status)
        buttonEnableService = findViewById(R.id.button_enable_service)
        textHelp = findViewById(R.id.text_help)

        buttonEnableService.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        if (isAccessibilityServiceEnabled(this, ClickService::class.java)) {
            textStatus.text = getString(R.string.service_status_enabled)
            buttonEnableService.isEnabled = false
            textHelp.visibility = TextView.GONE
        } else {
            textStatus.text = getString(R.string.service_status_disabled)
            buttonEnableService.isEnabled = true
            textHelp.visibility = TextView.VISIBLE
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
        val expectedComponentName = ComponentName(context, serviceClass)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)
        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName)
                return true
        }
        return false
    }
}

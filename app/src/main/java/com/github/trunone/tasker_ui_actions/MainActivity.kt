package com.github.trunone.tasker_ui_actions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity(), LogListener {

    private lateinit var textVersion: TextView
    private lateinit var textStatus: TextView
    private lateinit var buttonEnableService: Button
    private lateinit var textHelp: TextView
    private lateinit var switchEnableLogging: SwitchCompat
    private lateinit var spinnerLogLevel: Spinner
    private lateinit var buttonClearLogs: Button
    private lateinit var recyclerViewLogs: RecyclerView
    private lateinit var logAdapter: LogAdapter

    private var selectedMinLevel: LogLevel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        LogManager.init(this)

        textVersion = findViewById(R.id.text_version)
        textVersion.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)

        textStatus = findViewById(R.id.text_status)
        buttonEnableService = findViewById(R.id.button_enable_service)
        textHelp = findViewById(R.id.text_help)
        switchEnableLogging = findViewById(R.id.switch_enable_logging)
        spinnerLogLevel = findViewById(R.id.spinner_log_level)
        buttonClearLogs = findViewById(R.id.button_clear_logs)
        recyclerViewLogs = findViewById(R.id.recycler_view_logs)

        buttonEnableService.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        setupLogView()
    }

    private fun setupLogView() {
        logAdapter = LogAdapter()
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        recyclerViewLogs.layoutManager = layoutManager
        recyclerViewLogs.adapter = logAdapter

        switchEnableLogging.isChecked = LogManager.isLoggingEnabled
        switchEnableLogging.setOnCheckedChangeListener { _, isChecked ->
            LogManager.setLoggingEnabled(this, isChecked)
        }

        val levelOptions = arrayOf("ALL", LogLevel.DEBUG.label, LogLevel.INFO.label, LogLevel.WARN.label, LogLevel.ERROR.label)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, levelOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLogLevel.adapter = spinnerAdapter

        spinnerLogLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMinLevel = when (position) {
                    1 -> LogLevel.DEBUG
                    2 -> LogLevel.INFO
                    3 -> LogLevel.WARN
                    4 -> LogLevel.ERROR
                    else -> null
                }
                refreshLogs()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        buttonClearLogs.setOnClickListener {
            LogManager.clearLogs()
        }
    }

    private fun refreshLogs() {
        val logs = LogManager.getLogs(selectedMinLevel)
        logAdapter.setLogs(logs)
        if (logs.isNotEmpty()) {
            recyclerViewLogs.scrollToPosition(logs.size - 1)
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
        switchEnableLogging.isChecked = LogManager.isLoggingEnabled
        LogManager.addListener(this)
        refreshLogs()
    }

    override fun onPause() {
        super.onPause()
        LogManager.removeListener(this)
    }

    override fun onLogAdded(entry: LogEntry) {
        val minLevel = selectedMinLevel
        if (minLevel == null || entry.level.priority >= minLevel.priority) {
            logAdapter.addLog(entry)
            recyclerViewLogs.scrollToPosition(logAdapter.itemCount - 1)
        }
    }

    override fun onLogsCleared() {
        logAdapter.clearLogs()
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

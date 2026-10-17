package com.example.taskerplugin

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel(val priority: Int, val label: String) {
    VERBOSE(1, "VERBOSE"),
    DEBUG(2, "DEBUG"),
    INFO(3, "INFO"),
    WARN(4, "WARN"),
    ERROR(5, "ERROR")
}

data class LogEntry(
    val id: Long,
    val timestamp: Long,
    val level: LogLevel,
    val tag: String,
    val message: String
) {
    fun formattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

interface LogListener {
    fun onLogAdded(entry: LogEntry)
    fun onLogsCleared()
}

object LogManager {
    private const val MAX_LOGS = 1000
    private var nextId = 1L
    private val logs = mutableListOf<LogEntry>()
    private val listeners = mutableListOf<LogListener>()
    private var mainHandler: Handler? = null

    private fun getMainHandler(): Handler? {
        if (mainHandler == null) {
            try {
                if (Looper.getMainLooper() != null) {
                    mainHandler = Handler(Looper.getMainLooper())
                }
            } catch (e: Throwable) {
                // Handled for unit tests where Looper may not be mocked
            }
        }
        return mainHandler
    }

    fun addListener(listener: LogListener) {
        synchronized(this) {
            if (!listeners.contains(listener)) {
                listeners.add(listener)
            }
        }
    }

    fun removeListener(listener: LogListener) {
        synchronized(this) {
            listeners.remove(listener)
        }
    }

    fun log(level: LogLevel, tag: String, message: String) {
        try {
            when (level) {
                LogLevel.VERBOSE -> Log.v(tag, message)
                LogLevel.DEBUG -> Log.d(tag, message)
                LogLevel.INFO -> Log.i(tag, message)
                LogLevel.WARN -> Log.w(tag, message)
                LogLevel.ERROR -> Log.e(tag, message)
            }
        } catch (e: Throwable) {
            // Ignored in unit test environment where android.util.Log is not mocked
        }

        val entry: LogEntry
        val currentListeners: List<LogListener>
        synchronized(this) {
            entry = LogEntry(
                id = nextId++,
                timestamp = System.currentTimeMillis(),
                level = level,
                tag = tag,
                message = message
            )
            logs.add(entry)
            if (logs.size > MAX_LOGS) {
                logs.removeAt(0)
            }
            currentListeners = ArrayList(listeners)
        }

        val handler = getMainHandler()
        if (handler != null) {
            handler.post {
                for (listener in currentListeners) {
                    listener.onLogAdded(entry)
                }
            }
        } else {
            for (listener in currentListeners) {
                listener.onLogAdded(entry)
            }
        }
    }

    fun v(tag: String, message: String) = log(LogLevel.VERBOSE, tag, message)
    fun d(tag: String, message: String) = log(LogLevel.DEBUG, tag, message)
    fun i(tag: String, message: String) = log(LogLevel.INFO, tag, message)
    fun w(tag: String, message: String) = log(LogLevel.WARN, tag, message)
    fun e(tag: String, message: String) = log(LogLevel.ERROR, tag, message)

    fun getLogs(minLevel: LogLevel? = null): List<LogEntry> {
        synchronized(this) {
            if (minLevel == null || minLevel == LogLevel.VERBOSE) {
                return ArrayList(logs)
            }
            return logs.filter { it.level.priority >= minLevel.priority }
        }
    }

    fun clearLogs() {
        val currentListeners: List<LogListener>
        synchronized(this) {
            logs.clear()
            currentListeners = ArrayList(listeners)
        }

        val handler = getMainHandler()
        if (handler != null) {
            handler.post {
                for (listener in currentListeners) {
                    listener.onLogsCleared()
                }
            }
        } else {
            for (listener in currentListeners) {
                listener.onLogsCleared()
            }
        }
    }
}

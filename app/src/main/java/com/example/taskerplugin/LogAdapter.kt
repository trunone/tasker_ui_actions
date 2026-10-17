package com.example.taskerplugin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LogAdapter(
    private var logs: MutableList<LogEntry> = mutableListOf()
) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTimestamp: TextView = view.findViewById(R.id.text_log_timestamp)
        val textLevel: TextView = view.findViewById(R.id.text_log_level)
        val textTag: TextView = view.findViewById(R.id.text_log_tag)
        val textMessage: TextView = view.findViewById(R.id.text_log_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log_entry, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val entry = logs[position]
        holder.textTimestamp.text = entry.formattedTime()
        holder.textLevel.text = "[${entry.level.label}]"
        holder.textTag.text = entry.tag
        holder.textMessage.text = entry.message

        val levelColor = when (entry.level) {
            LogLevel.VERBOSE -> Color.parseColor("#757575")
            LogLevel.DEBUG -> Color.parseColor("#1976D2")
            LogLevel.INFO -> Color.parseColor("#388E3C")
            LogLevel.WARN -> Color.parseColor("#F57C00")
            LogLevel.ERROR -> Color.parseColor("#D32F2F")
        }
        holder.textLevel.setTextColor(levelColor)
    }

    override fun getItemCount(): Int = logs.size

    fun setLogs(newLogs: List<LogEntry>) {
        logs.clear()
        logs.addAll(newLogs)
        notifyDataSetChanged()
    }

    fun addLog(entry: LogEntry) {
        logs.add(entry)
        notifyItemInserted(logs.size - 1)
    }

    fun clearLogs() {
        logs.clear()
        notifyDataSetChanged()
    }
}

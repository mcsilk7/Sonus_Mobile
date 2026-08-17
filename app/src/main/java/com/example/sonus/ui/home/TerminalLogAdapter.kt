package com.example.sonus.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sonus.R

class TerminalLogAdapter : RecyclerView.Adapter<TerminalLogAdapter.LogViewHolder>() {

    private val logs = mutableListOf<String>()

    class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLine: TextView = view.findViewById(R.id.tvLogLine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_terminal_log, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.tvLine.text = logs[position]
    }

    override fun getItemCount() = logs.size

    fun addLog(line: String) {
        logs.add("> $line")
        notifyItemInserted(logs.size - 1)
        if (logs.size > 50) { // Keep only last 50 lines
            logs.removeAt(0)
            notifyItemRemoved(0)
        }
    }

    fun setLogs(newLogs: List<String>) {
        logs.clear()
        logs.addAll(newLogs.map { "> $it" })
        notifyDataSetChanged()
    }
}

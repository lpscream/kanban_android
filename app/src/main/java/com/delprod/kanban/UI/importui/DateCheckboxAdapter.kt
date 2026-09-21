package com.delprod.kanban.UI.importui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delprod.kanban.R
import com.delprod.kanban.data.DateGroup
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Показує список дат зі звіту у вигляді чекбоксів.
 * [onToggle] викликається при зміні стану чекбокса конкретної дати.
 */
class DateCheckboxAdapter(
    private val onToggle: (LocalDate) -> Unit
) : RecyclerView.Adapter<DateCheckboxAdapter.ViewHolder>() {

    private val dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    private var dateGroups: List<DateGroup> = emptyList()
    private var selectedDates: Set<LocalDate> = emptySet()

    fun submit(dateGroups: List<DateGroup>, selectedDates: Set<LocalDate>) {
        this.dateGroups = dateGroups
        this.selectedDates = selectedDates
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkbox: CheckBox = view.findViewById(R.id.checkboxDate)
        val dateLabel: TextView = view.findViewById(R.id.dateLabel)
        val countLabel: TextView = view.findViewById(R.id.subdivisionsCountLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_date_checkbox, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = dateGroups[position]
        holder.dateLabel.text = group.date.format(dateFmt)
        holder.countLabel.text = "(підрозділів: ${group.subdivisions.size})"

        // Знімаємо попередній listener, щоб уникнути помилкових спрацювань при переюзанні view
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = group.date in selectedDates
        holder.checkbox.setOnCheckedChangeListener { _, _ -> onToggle(group.date) }
    }

    override fun getItemCount(): Int = dateGroups.size
}

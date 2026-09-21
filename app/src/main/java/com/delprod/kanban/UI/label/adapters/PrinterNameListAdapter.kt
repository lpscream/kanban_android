package com.delprod.kanban.UI.label.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.delprod.kanban.databinding.PrinterNameListItemBinding
import com.delprod.kanban.db.PrinterSettings

class PrinterNameListAdapter(
    private val item: List<PrinterSettings>,
    private val onPrinterClicked: (PrinterSettings) -> Unit
): RecyclerView.Adapter<PrinterNameListAdapter.PrinterNameListViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PrinterNameListViewHolder {
        val binding = PrinterNameListItemBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false)
        return PrinterNameListViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: PrinterNameListViewHolder,
        position: Int
    ) {
        holder.bind(item[position])
    }

    override fun getItemCount(): Int = item.size


    inner class PrinterNameListViewHolder(private val binding: PrinterNameListItemBinding):
            RecyclerView.ViewHolder(binding.root){
        fun bind(item: PrinterSettings){
            binding.labelName.text = item.name
            binding.root.setOnClickListener {
                onPrinterClicked(item)
            }
        }
            }
}
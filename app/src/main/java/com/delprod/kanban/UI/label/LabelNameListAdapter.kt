package com.delprod.kanban.UI.label

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.delprod.kanban.R
import com.delprod.kanban.databinding.LabelNameListItemBinding
import com.delprod.kanban.db.LabelData

class LabelNameListAdapter(
    private val items: List<LabelData>,
    private val onClickItem: (LabelData) -> Unit,
    private val onEditLabel: (LabelData) -> Unit,
    private val onFastEditLabel: (LabelData) -> Unit,
    private val onAddOnBasicLabel: (LabelData) -> Unit
) : RecyclerView.Adapter<LabelNameListAdapter.LabelNameListViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LabelNameListViewHolder {
        val binding =
            LabelNameListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return LabelNameListViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: LabelNameListViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size


    inner class LabelNameListViewHolder(private val binding: LabelNameListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LabelData) {
            binding.labelName.setText("${item.code}, ${item.labelName}")
            binding.root.setOnClickListener {
                onClickItem(item)
            }
            binding.root.setOnLongClickListener {
                showPopupMenu(binding.labelName, item)
                return@setOnLongClickListener true
            }
        }

        private fun showPopupMenu(view: View, item: LabelData){
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.label_item_menu, popup.menu)
            popup.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.action_edit_label -> onEditLabel(item)
                    R.id.action_add_on_the_basic_label -> onAddOnBasicLabel(item)
                    R.id.action_fast_edit_label -> onFastEditLabel(item)
                }
                true
            }
            popup.show()
        }
    }
}
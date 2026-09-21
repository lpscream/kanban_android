package com.delprod.kanban.UI.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.delprod.kanban.databinding.OrderListItemBinding
import com.delprod.kanban.db.OrderList

class OrdersItemsListAdapter(
    private val items: List<OrderList>,
    private val onClickItem: (String) -> Unit
    ): RecyclerView.Adapter<OrdersItemsListAdapter.OrdersItmsListViwHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrdersItmsListViwHolder {
        val binding =
            OrderListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false)
        return OrdersItmsListViwHolder(binding)
    }

    override fun onBindViewHolder(
        holder: OrdersItmsListViwHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int =items.size


    inner class OrdersItmsListViwHolder(private val binding: OrderListItemBinding):
    RecyclerView.ViewHolder(binding.root){
        fun bind(item: OrderList){
            binding.orderName.setText("${item.subdivisionCode}")
            binding.orderDate.setText("${item.dateEpochDay}")
            binding.root.setOnClickListener {
                onClickItem(item.uuid)
            }
        }
    }
}
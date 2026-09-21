package com.delprod.kanban.UI.placing.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.delprod.kanban.data.models.ProductListItem
import com.delprod.kanban.databinding.ProductListItemBinding
import com.delprod.kanban.utils.EditTextExtentions.addPriceChangeListener

class PlacingProductListAdapter(
    private val items: List<ProductListItem>
): RecyclerView.Adapter<PlacingProductListAdapter.PlacingProductListViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlacingProductListViewHolder {
        val binding = ProductListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlacingProductListViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: PlacingProductListViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun fetchItemList() = items

    inner class PlacingProductListViewHolder(private val binding: ProductListItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: ProductListItem){
            binding.name.setText(item.name)
            binding.article.setText(item.code)
            binding.pluCode.setText(item.plu)
            binding.quantityOrderedEditText.addPriceChangeListener(6) {
                item.quantityOrdered = it
            }
        }
    }
}
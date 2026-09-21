package com.delprod.kanban.UI.orders

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.delprod.kanban.R
import com.delprod.kanban.core.logPrint
import com.delprod.kanban.data.goods.BarcodeData
import com.delprod.kanban.data.goods.ProductModel
import com.delprod.kanban.databinding.OrdersItemLayoutBinding
import com.delprod.kanban.db.OrderLineWithDetails
import com.delprod.kanban.utils.weightGramsToKg

class ProductsItemsAdapter(
    private val items: List<OrderLineWithDetails>,
    private val onClick: (OrderLineWithDetails) -> Unit,
    private val onCleanExecuted: (OrderLineWithDetails) -> Unit,
    private val onDeleteProduct: (OrderLineWithDetails) -> Unit
): RecyclerView.Adapter<ProductsItemsAdapter.ProductsItemsViewHolder> (){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductsItemsViewHolder {
        val binding = OrdersItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return ProductsItemsViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ProductsItemsViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun fetchItems(): List<OrderLineWithDetails>{
        val itemsList: List<OrderLineWithDetails> = listOf()
        itemsList.plus(items)
        return  items
    }

    fun fetchItem(barcode: BarcodeData): OrderLineWithDetails? {
        val item = items.find { it.nomenclatureCode == barcode.article.toString() }
        return item
    }

    inner class ProductsItemsViewHolder(
        private val binding: OrdersItemLayoutBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun bind(item: OrderLineWithDetails){
            binding.root.setBackgroundResource(R.drawable.bg_card_glass)
            binding.weightFlaw.setTextColor(binding.root.context.resources.getColor(R.color.black))

            val weightFlaw = (item.quantityOrdered.let { it ?: 0 }) - (item.quantityExecuted.let { it ?: 0 })
            if (weightFlaw <= 0){
                binding.root.setBackgroundResource(R.drawable.bg_gradient_green)
                binding.weightFlaw.setTextColor(binding.root.resources.getColor(R.color.red))
                binding.quantityFlaw.setTextColor(binding.root.resources.getColor(R.color.red))
            }
            binding.name.setText(item.nomenclatureName)
            binding.weightOrdered.setText("${item.quantityOrdered.let { it?.weightGramsToKg() ?: "0.00" }} кг.")
            binding.weightExecuted.setText("${item.quantityExecuted.let { it?.weightGramsToKg() ?: "0.00" }} кг.")
            binding.weightFlaw.setText("${weightFlaw.weightGramsToKg()} кг.")
            binding.quantity.setText("${item.boxSum.let { it ?: "0" }} шт.")

     try {
         val averageWeight = item.quantityExecuted.let { it?.weightGramsToKg() ?: "0.00" }.toDouble() / item.boxSum.let { it ?: 0 }
         val averageBoxQuantitySum = weightFlaw.toDouble() / averageWeight
         binding.quantityFlaw.text = averageBoxQuantitySum.toInt().weightGramsToKg()
     }catch (e: Exception){
         logPrint(e.message.toString())
     }


            binding.root.setOnClickListener {
                onClick(item)
            }
            binding.root.setOnLongClickListener {
                showPopupMenu(binding.name, item)
                return@setOnLongClickListener true
            }
        }

        private fun showPopupMenu(view: View, item: OrderLineWithDetails){
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.order_item_menu, popup.menu)
            popup.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.action_delete -> { onDeleteProduct(item)}
                    R.id.action_clean -> {onCleanExecuted(item)}
                }
                true
            }
            popup.show()
        }
    }
}
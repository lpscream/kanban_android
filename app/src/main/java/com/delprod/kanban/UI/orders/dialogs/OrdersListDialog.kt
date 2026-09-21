package com.delprod.kanban.UI.orders.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.delprod.kanban.UI.orders.OrdersItemsListAdapter
import com.delprod.kanban.databinding.OrdersListDialogLayoutBinding
import com.delprod.kanban.db.OrderList

object OrdersListDialog {

    fun create(
        context: Context,
        inflater: LayoutInflater,
        onItemSelected: (String) -> Unit,
        listItems: List<OrderList>
    ){
        val binding by lazy { OrdersListDialogLayoutBinding.inflate(inflater) }
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setView(binding.root)
            .setCancelable(true)
        val dialog = dialogBuilder.create()
        dialog.show()



        if (listItems == null){
            //show message to add new items
            binding.ordersList.isVisible = false
            binding.emptyListMessage.isVisible = true
        }else{
            val adapter = OrdersItemsListAdapter(listItems) {

                onItemSelected(it)
                dialog.dismiss()
            }
            binding.ordersList.isVisible = true
            binding.emptyListMessage.isVisible = false
            binding.ordersList.layoutManager = LinearLayoutManager(context)
            binding.ordersList.adapter = adapter
        }
    }
}
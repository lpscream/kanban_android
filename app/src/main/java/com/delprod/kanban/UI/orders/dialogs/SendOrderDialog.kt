package com.delprod.kanban.UI.orders.dialogs

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import com.delprod.kanban.UI.settings.SettingsViewModel
import com.delprod.kanban.core.logPrint
import com.delprod.kanban.data.Settings
import com.delprod.kanban.data.goods.Order
import com.delprod.kanban.databinding.SendOrderDialogBinding
import com.delprod.kanban.db.OrderLineWithDetails
import com.delprod.kanban.utils.PrepareOrder

object SendOrderDialog {
    fun create(
        context: Context,
        viewModel: SettingsViewModel,
        order: List<OrderLineWithDetails>,
        inflater: LayoutInflater,
        onSendButtonPressed : () -> Unit
    ){
        val binding by lazy { SendOrderDialogBinding.inflate(inflater) }
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setView(binding.root)
            .setCancelable(true)
        val dialog = dialogBuilder.create()
        dialog.show()

//        binding.sentBtn.isEnabled = false

        binding.sentBtn.setOnClickListener {

            if (order != null){
                viewModel.sendOrder(PrepareOrder().buildMessageHTML(order, binding.comment.text.toString()))
                dialog.dismiss()
            }
        }



    }
}
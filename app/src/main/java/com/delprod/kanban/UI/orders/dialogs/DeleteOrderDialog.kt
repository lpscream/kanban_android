package com.delprod.kanban.UI.orders.dialogs

import android.app.AlertDialog
import android.content.Context
import com.delprod.kanban.R

object DeleteOrderDialog {
    fun create(
        context: Context,
        onDeleteButtonPressed : () -> Unit
    ){
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setMessage(context.resources.getString(R.string.delete_alert_dialog_attention_message))
            .setPositiveButton(context.resources.getString(R.string.yes_btn_sign)){ dialog, id ->
                onDeleteButtonPressed()
            }
            .setNegativeButton(context.resources.getString(R.string.no_btn_sign)){ dialog, id ->
                dialog.dismiss()
            }
        val dialog = dialogBuilder.create()
        dialog.show()
    }
}
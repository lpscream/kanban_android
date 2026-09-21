package com.delprod.kanban.UI.settings

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.delprod.kanban.R
import com.delprod.kanban.data.Settings
import com.delprod.kanban.databinding.PasswordDialogLayoutBinding

object PasswordDialog {
    fun create(
        context: Context,
        inflater: LayoutInflater,
        settings: Settings,
        onPassCorrect: () -> Unit
    ){
        val binding by lazy { PasswordDialogLayoutBinding.inflate(inflater) }
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setView(binding.root)
            .setCancelable(true)
        val dialog = dialogBuilder.create()
        dialog.show()

        binding.btnDeny.setOnClickListener { dialog.dismiss() }

        binding.btnAccept.setOnClickListener {
            if (settings.password() == binding.inputBox.text.toString()){
                onPassCorrect()
                dialog.dismiss()
            }else binding.message.setText(context.resources.getString(R.string.password_error))
        }
    }
}
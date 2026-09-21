package com.delprod.kanban.UI.placing

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.delprod.kanban.UI.clients.adapter.ClientsAdapter
import com.delprod.kanban.databinding.PlacingClientListDialogBinding
import com.delprod.kanban.db.SubdivisionEntity

object PlacingClientDialog {
    fun create(
        context: Context,
        clientList: List<SubdivisionEntity>,
        onClick: (SubdivisionEntity) -> Unit
    ){
        val binding by lazy { PlacingClientListDialogBinding.inflate(LayoutInflater.from(context)) }
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setView(binding.root)
            .setCancelable(true)
        val dialog = dialogBuilder.create()
        dialog.show()

        val adapter = ClientsAdapter(clientList){
            onClick(it)
            dialog.dismiss()
        }

        binding.clientList.layoutManager = LinearLayoutManager(context)
        binding.clientList.adapter = adapter
    }
}
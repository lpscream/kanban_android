package com.delprod.kanban.UI.label.dialogs

import android.app.AlertDialog
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.delprod.kanban.R
import com.delprod.kanban.UI.label.LabelViewModel
import com.delprod.kanban.UI.label.PrintLabelActivity
import com.delprod.kanban.UI.label.PrinterListUiState
import com.delprod.kanban.UI.label.adapters.PrinterNameListAdapter
import com.delprod.kanban.databinding.PrintLabelDialogBinding
import com.delprod.kanban.db.PrinterSettings
import com.delprod.kanban.utils.launchAndCollectIn

object PrintLabelDialog {
    fun create(
        context: PrintLabelActivity,
        inflater: LayoutInflater,
        viewModel: LabelViewModel,
        onPrint: (PrinterSettings) -> Unit
    ){
        val binding by lazy { PrintLabelDialogBinding.inflate(inflater) }
        val dialogBuilder = AlertDialog.Builder(context)
            dialogBuilder.setView(binding.root)
            .setCancelable(true)
        val dialog = dialogBuilder.create()
        dialog.show()


        viewModel.fetchPtrinterListNames()
        viewModel.uiPrinterNamesState.launchAndCollectIn(context){
            when(it){
                is PrinterListUiState.Idle -> {}
                is PrinterListUiState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.printerNameList.isVisible = false
                    binding.messageTv.isVisible = false
                }
                is PrinterListUiState.Error -> {
                    binding.progressBar.isVisible = false
                    binding.printerNameList.isVisible = false
                    binding.messageTv.isVisible = true
                    binding.messageTv.setText(it.message.toString())
                }
                is PrinterListUiState.Loaded -> {
                    if (it.printerNames.isEmpty()){
                        binding.progressBar.isVisible = false
                        binding.printerNameList.isVisible = false
                        binding.messageTv.isVisible = true
                        binding.messageTv.setText(binding.root.context.resources.getString(R.string.empty_printer_list_message))
                    }else{
                        binding.progressBar.isVisible = false
                        binding.printerNameList.isVisible = true
                        binding.messageTv.isVisible = false
                        //adapter
                        val adapter = PrinterNameListAdapter(it.printerNames){
                            onPrint(it)
                            dialog.dismiss()
                        }
                        binding.printerNameList.layoutManager = LinearLayoutManager(context)
                        binding.printerNameList.adapter = adapter
                    }
                }
            }
        }

    }
}
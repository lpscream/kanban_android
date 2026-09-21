package com.delprod.kanban.UI.label.dialogs

import android.app.AlertDialog
import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.delprod.kanban.UI.label.LabelListActivity
import com.delprod.kanban.UI.label.LabelViewModel
import com.delprod.kanban.databinding.AddPrinterDialogBinding
import com.delprod.kanban.db.PrinterSettings

object AddPrinterDialog {
    fun create(
        context: LabelListActivity,
        inflater: LayoutInflater,
        viewModel: LabelViewModel,
        printer: PrinterSettings?,
        onSave: () -> Unit
    ){
        val binding by lazy { AddPrinterDialogBinding.inflate(inflater) }
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setView(binding.root)
            .setCancelable(true)
        val dialog  = dialogBuilder.create()
        dialog.show()


        if (printer == null){
            binding.daletePrinterBtn.isVisible = false
        }else{
            binding.printerName.setText(printer.name)
            binding.printerAddress.setText(printer.ip)
            binding.printerPort.setText(printer.port)
            binding.daletePrinterBtn.isVisible = true
            binding.daletePrinterBtn.setOnClickListener {
                viewModel.deletePrinter(printer)
                onSave()
                dialog.dismiss()
            }
        }
        binding.savePrinterBtn.setOnClickListener {
            val printerConfig = PrinterSettings(
                name = binding.printerName.text.toString(),
                ip = binding.printerAddress.text.toString(),
                port = binding.printerPort.text.toString()
            )

            if (printer == null){
                viewModel.insertPrinter(PrinterSettings(
                    name = binding.printerName.text.toString(),
                    ip = binding.printerAddress.text.toString(),
                    port = binding.printerPort.text.toString()
                ))
            }else{
                //update printer settings
                viewModel.updatePrinter(PrinterSettings(
                    id = printer.id,
                    name = binding.printerName.text.toString(),
                    ip = binding.printerAddress.text.toString(),
                    port = binding.printerPort.text.toString()
                ))
            }
            onSave
            dialog.dismiss()
        }


    }
}
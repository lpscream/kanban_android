package com.delprod.kanban.UI.orders.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.delprod.kanban.core.logPrint
import com.delprod.kanban.data.Settings
import com.delprod.kanban.data.goods.BarcodeData
import com.delprod.kanban.data.goods.BarcodeIdType
import com.delprod.kanban.databinding.AddWeightToProductDialogBinding
import com.delprod.kanban.db.OrderLineWithDetails
import com.delprod.kanban.utils.EditTextExtentions.addPriceChangeListener
import com.delprod.kanban.utils.EditTextExtentions.dateFormatValidation
import com.delprod.kanban.utils.kToGrams

object AddWeightDateToProduct {
    fun create(
        context: Context,
        item: OrderLineWithDetails,
        barcodeData: BarcodeData?,
        settings: Settings,
        onAddBtnPressed: (BarcodeData) -> Unit
    ){
        val binding by lazy { AddWeightToProductDialogBinding.inflate(LayoutInflater.from(context)) }
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setView(binding.root)
            .setCancelable(true)
        val dialog = dialogBuilder.create()
        dialog.show()

        var isDateValid = false



        binding.saveBtn.setOnClickListener {
            if (isDateValid){
                onAddBtnPressed(BarcodeData(
                    barIdCodeType = BarcodeIdType.ARTICLE,
                    article = item.nomenclatureCode.toInt(),
                    weightKg = binding.weightToCalculate.text.toString().kToGrams(),
                    date = binding.dateToCalculate.text.toString()
                ))
                dialog.dismiss()
            }
        }

        if (barcodeData == null){
            settings.lastDate().let {
                isDateValid = true
                settings.lastWeight().let { binding.weightToCalculate.setText(it) }
                binding.dateToCalculate.setText(it) }
        }else {
            isDateValid = true
            binding.dateToCalculate.setText(barcodeData.date)
            binding.weightToCalculate.setText(barcodeData.weightKg.toString())
        }




        binding.itemName.setText(item.nomenclatureName)

        binding.dateToCalculate.dateFormatValidation {
            if (it) settings.lastDate(binding.dateToCalculate.text.toString())
            isDateValid = it
        }

        binding.weightToCalculate.addPriceChangeListener(6){
            settings.lastWeight(binding.weightToCalculate.text.toString())
            logPrint(it)
        }
    }
}
package com.delprod.kanban.UI.label.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.delprod.kanban.UI.label.labelFieldMapper
import com.delprod.kanban.UI.label.label_utils.DefaultLabelData
import com.delprod.kanban.core.logPrint
import com.delprod.kanban.databinding.AddLabelSimpleDialogBinding
import com.delprod.kanban.db.LabelData

object SimpleLabelEditorDialog {
    fun create(
        context: Context,
        label: List<LabelData>?,
        onInsert: (List<LabelData>) -> Unit,
        onUpdate: (List<LabelData>) -> Unit
    ){
        val binding by lazy { AddLabelSimpleDialogBinding.inflate(LayoutInflater.from(context)) }
        val dialogBuilder = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(true)
        val dialog = dialogBuilder.create()
        dialog.show()

        binding.saveBtn.setOnClickListener {
            if (label == null){
                onInsert(labelIsNull(binding) )
            }else {
                onUpdate(labelIsNotNull(binding, label as MutableList))
            }
            dialog.dismiss()
        }

        if (label == null){
            fillViewFields(labelFieldMapper(DefaultLabelData.createDefaultFields()) as MutableList, binding)
        }else{
            fillViewFields(label as MutableList, binding)
        }
    }

    private fun labelIsNotNull(binding: AddLabelSimpleDialogBinding, label: MutableList<LabelData>): MutableList<LabelData> =
        fillLabel(label, binding)

    private fun labelIsNull(binding: AddLabelSimpleDialogBinding): List<LabelData> =
        fillLabel(labelFieldMapper(DefaultLabelData.createDefaultFields()) as MutableList, binding) as List<LabelData>

    private fun fillLabel(label: MutableList<LabelData>, binding: AddLabelSimpleDialogBinding): MutableList<LabelData>{
       label.forEach { line ->
           line.code = binding.article.text.toString()
           line.labelName = binding.labelName.text.toString()
            when{
                line.uniqueName == "name" -> {
                    line.text = binding.name.text.toString()
                }
                line.uniqueName == "dstu" -> {
                    line.text = binding.dstu.text.toString()
                }
                line.uniqueName == "nutrition" -> {
                    line.text = binding.nutrition.text.toString()
                }
                line.uniqueName == "storage" -> {
                    line.text = binding.storageTime.text.toString()
                }
                line.uniqueName == "weight" -> {
                    if (binding.isWeight.isChecked){
                        line.text = "12.500"
                        line.fontSizeMm = 5.5f
                    } else {
                        line.text = "00000"
                        line.fontSizeMm = 0.0f
                    }
                }
                line.uniqueName == "weightSign" -> {
                    if (binding.isWeight.isChecked){
                        line.fontSizeMm = 4.0f
                    }else {
                        line.fontSizeMm = 0.0f
                    }
                }
                line.uniqueName =="expDays" -> {
                    line.text = binding.storageDays.text.toString()
                }

            }
        }
        label.forEach {  logPrint(it.toString()) }
        return label
    }

    private fun fillViewFields(label: List<LabelData>, binding: AddLabelSimpleDialogBinding){
        label.forEach {
            when{
                it.uniqueName == "name" -> {
                    binding.article.setText(it.code)
                    binding.name.setText(it.text)
                    binding.labelName.setText(it.labelName)
                }
                it.uniqueName == "dstu" -> {
                    binding.dstu.setText(it.text)
                }
                it.uniqueName == "nutrition" -> {
                    binding.nutrition.setText(it.text)
                }
                it.uniqueName == "storage" -> {
                    binding.storageTime.setText(it.text)
                }
                it.uniqueName == "weight" -> {
                        binding.isWeight.isChecked = it.text != "00000"

                }
                it.uniqueName == "expDays" -> {
                    binding.storageDays.setText(it.text)
                }
            }
        }
    }
}
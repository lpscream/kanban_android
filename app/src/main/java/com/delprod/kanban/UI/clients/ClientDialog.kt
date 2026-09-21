package com.delprod.kanban.UI.clients

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.delprod.kanban.databinding.ClientAddingDialogBinding
import com.delprod.kanban.db.SubdivisionEntity

object ClientDialog {

    fun create(
        context: Context,
        inflater: LayoutInflater,
        item: SubdivisionEntity?,
        onSaveBtnPressed: (SubdivisionEntity) -> Unit,
        onDeletePressed: (SubdivisionEntity) -> Unit
    ){
        val binding by lazy { ClientAddingDialogBinding.inflate(inflater) }
        val dialogBuilder = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(true)
        val dialog = dialogBuilder.create()
        dialog.show()


            binding.deleteBtn.isVisible = (item != null)
            binding.clientNumber.isEnabled = false
            binding.clientCode.isEnabled = (item == null)

        binding.clientNumber.setText(item?.id?.toString() ?: "")
        binding.clientCode.setText(item?.code?.toString() ?: "")
        binding.clientFullLabel.setText(item?.fullLabel?.toString() ?: "")
        binding.clientRegion.setText(item?.region?.toString() ?: "")
        binding.clientResponsiblePerson.setText(item?.responsiblePerson?.toString() ?: "")
        binding.clientPhone.setText(item?.phone?.toString() ?: "")
        binding.clientUnitId.setText(item?.unitId?.toString() ?: "")
        binding.clientCategory.setText(item?.category?.toString() ?: "")


        binding.saveBtn.setOnClickListener {
            onSaveBtnPressed(SubdivisionEntity(
                id = item?.id ?: 0,
                code = binding.clientCode.text.toString(),
                fullLabel = binding.clientFullLabel.text.toString(),
                region = binding.clientRegion.text.toString(),
                responsiblePerson =  binding.clientResponsiblePerson.text.toString(),
                phone = binding.clientPhone.text.toString(),
                unitId = binding.clientUnitId.text.toString(),
                category = binding.clientCategory.text.toString()
            ))
            dialog.dismiss()
        }

        if (item != null){
            binding.deleteBtn.setOnClickListener {
                onDeletePressed(item)
                dialog.dismiss()
            }
        }
    }
}
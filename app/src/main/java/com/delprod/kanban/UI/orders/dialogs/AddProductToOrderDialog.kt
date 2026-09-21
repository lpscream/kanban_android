package com.delprod.kanban.UI.orders.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.delprod.kanban.R
import com.delprod.kanban.UI.goods.GoodsAdapter
import com.delprod.kanban.core.logPrint
import com.delprod.kanban.databinding.AddProductToOrderDialogLayoutBinding
import com.delprod.kanban.db.NomenclatureEntity
import com.delprod.kanban.db.OrderLineEntity
import com.delprod.kanban.db.OrderLineWithDetails
import com.delprod.kanban.utils.EditTextExtentions.addPriceChangeListener

object AddProductToOrderDialog {
    fun create(
        context: Context,
        inflater: LayoutInflater,
        orderItemsList: List<OrderLineWithDetails>,
        itemList: List<NomenclatureEntity>,
        onSaveButtonPressed: (OrderLineEntity) -> Unit
    ) {
        val binding by lazy { AddProductToOrderDialogLayoutBinding.inflate(inflater) }
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setView(binding.root)
            .setCancelable(false)
        val dialog = dialogBuilder.create()
        dialog.show()

        binding.decline.setOnClickListener {
            dialog.dismiss()
        }

        binding.accept.setOnClickListener {
            val item = orderItemsList.get(0)
            onSaveButtonPressed(
                OrderLineEntity(
                    dateEpochDay = item.dateEpochDay,
                    subdivisionId = item.subdivisionId,
                    nomenclatureId = binding.itemName.tag.toString().toLong(),
                    quantityOrdered = (binding.weight.text.toString().toDouble() * 1000).toInt()
                        ?: 0,
                    quantityExecuted = 0,
                    boxSum = 0,
                    dateList = "",
                    sourceFileName = null,
                    importedAtMillis = null,
                    uuid = item.uuid
                )
            )
            dialog.dismiss()
        }


        binding.weight.addPriceChangeListener(6) {
            logPrint(it)
//            binding.accept.isEnabled = isValid(binding)
        }


        initAdapter(
            itemList,
            binding,
            orderItemsList
        ) {code, name ->
            binding.itemName.setText(name)
            binding.itemName.tag = code
        }
        binding.accept.isEnabled = false
    }

    private fun initAdapter(
        itemList: List<NomenclatureEntity>,
        binding: AddProductToOrderDialogLayoutBinding,
        orderItemsList: List<OrderLineWithDetails>,
        onItemSelected: (String, String) -> Unit
    ) {
        val adapter = GoodsAdapter(itemList) { id, item ->
            if (orderItemsList.any { it.nomenclatureCode == item.code }) {
                binding.itemName.setText(binding.root.context.resources.getString(R.string.double_error))
                binding.accept.isEnabled = false
            } else {
                binding.itemName.tag = item.id
                binding.itemName.setText("${item.code}, ${item.name}")
                binding.accept.isEnabled = true
            }
        }
        binding.itemsList.layoutManager = LinearLayoutManager(binding.root.context)
        binding.itemsList.adapter = adapter
    }


    private fun isValid(binding: AddProductToOrderDialogLayoutBinding): Boolean{
        return if (binding.weight.text.length > 3
            && binding.itemName.tag.toString().length > 0
            && binding.itemName.text.length > 0){
            true
        }else false
    }
}
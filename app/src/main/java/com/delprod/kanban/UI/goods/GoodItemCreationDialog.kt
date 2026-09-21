package com.delprod.kanban.UI.goods

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.delprod.kanban.R
import com.delprod.kanban.core.logPrint
import com.delprod.kanban.data.goods.ProductModel
import com.delprod.kanban.databinding.AddGoodItemDialogLayoutBinding
import com.delprod.kanban.db.NomenclatureEntity

object GoodItemCreationDialog {
    fun create(
        context: Context,
        inflater: LayoutInflater,
        item:  NomenclatureEntity?,
        onSaveButtonPressed : (NomenclatureEntity) -> Unit,
        onUpdateItem: (NomenclatureEntity) -> Unit
    ){
            val binding by lazy{ AddGoodItemDialogLayoutBinding.inflate(inflater) }
            val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setView(binding.root)
            .setCancelable(true)
        val dialog = dialogBuilder.create()
        dialog.show()

        binding.article.visibility = View.GONE

        if (item != null){
            binding.nomeclatureCode.setText(item.code)
            binding.pluCode.setText(item.plu)
            binding.nomeclatureCode.isEnabled = false
            binding.name.setText(item.name)
            binding.addGoodItemToMemory.setText(context.resources.getString(R.string.update_btn))
        }

        binding.addGoodItemToMemory.setOnClickListener {
            when{
                binding.name.text.toString().isNullOrEmpty()  ->{
                    Toast.makeText(context, "Введите наименование товара", Toast.LENGTH_LONG).show()
                }
                binding.name.text.toString().length < 5 -> {
                    Toast.makeText(context, "Введите наименование длиннее", Toast.LENGTH_LONG).show()
                }
                binding.nomeclatureCode.text.toString().isNullOrEmpty() -> {
                    Toast.makeText(context, "Введите артикул товара", Toast.LENGTH_LONG).show()
                }
                else -> {
                    if (item == null){
                        logPrint("save item")
                        try {
                            onSaveButtonPressed(NomenclatureEntity(
                                code = binding.nomeclatureCode.text.toString(),
                                name = binding.name.text.toString(),
                                unit = "кг",
                                plu = binding.pluCode.text.toString()))
                            dialog.dismiss()
                        }catch (e: Exception){
                            logPrint("error message: $e")
                            Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
                        }
                    }else{
                        logPrint("update item")
                        try {

                            onUpdateItem(NomenclatureEntity(
                                code = binding.nomeclatureCode.text.toString(),
                                name = binding.name.text.toString(),
                                unit = "кг",
                                plu = binding.pluCode.text.toString()))
                            dialog.dismiss()
                        }catch (e: Exception){
                            logPrint("error message: $e")
                            Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}
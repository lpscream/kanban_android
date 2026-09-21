package com.delprod.kanban.utils

import com.delprod.kanban.core.logPrint
import com.delprod.kanban.data.goods.Order
import com.delprod.kanban.db.OrderLineWithDetails
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class PrepareOrder {


    fun buildMessageHTML(orders: List<OrderLineWithDetails>, comment: String?): String {
        val stringBuilder = StringBuilder()


        stringBuilder.append("Дата сборки: ${orders[0].dateEpochDay}\n")
            .append("<b>Покупатель: ${orders[0].subdivisionCode?.uppercase()}</b>\n")

        if (comment != null){
            stringBuilder.append("Комментарий: ${comment}\n")
        }
            
//        if(orders.comment != "") stringBuilder.append("Комментарий: ${orders.comment}\n")

        var commonWeight = 0
        var commonBoxSum = 0

        orders.forEach { item ->
            val datesBuilder = StringBuilder()
            logPrint("item.dateList: ${item.dateList}")
            parseDateList(item.dateList).forEach { datesBuilder.append(" $it ") }


            stringBuilder
                .append("\n")
                .append("<b>${item.nomenclatureCode} ${item.nomenclatureName}</b>\n")
                .append("Вес: ${item.quantityExecuted?.weightGramsToKg()} кг. ")
                .append("(${item.boxSum} шт.)\n")
                .append("$datesBuilder\n")
            if (item.quantityExecuted != null){
                commonWeight = commonWeight + item.quantityExecuted
            }

            if (item.boxSum != null){
                commonBoxSum = commonBoxSum + item.boxSum
            }
        }

        stringBuilder.append("-------------------------------\n\n")
            .append("Общий вес заказа: ${commonWeight.weightGramsToKg()} кг.\n")
            .append("Количество ящиков: ${commonBoxSum} шт.")

        return stringBuilder.toString()
    }




    private fun parseDateList(dates: String?): List<String>{
        logPrint("dates: $dates")
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        return if (dates != null){
            val type = object : TypeToken<List<String>>() {}.type
            val items: List<String> = gson.fromJson(dates, type)
            items.distinct()
        }else listOf()
    }
}
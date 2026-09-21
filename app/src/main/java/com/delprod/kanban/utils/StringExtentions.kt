package com.delprod.kanban.utils

import com.delprod.kanban.core.logPrint
import com.delprod.kanban.data.goods.BarcodeData
import com.delprod.kanban.data.goods.BarcodeIdType
import com.delprod.kanban.db.ExportData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.apache.xmlbeans.XmlCursor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Int.weightGramsToKg(): String {
    val weightKg: Double = this / 1000.000
    return weightKg.toString()
}


fun String.kToGrams(): Int {
    val quantity = this.toDouble()
    return (quantity * 1000).toInt()
}

fun String.parseBarcode(
    onSuccess: (BarcodeData) -> Unit,
    onFailure: (String) -> Unit
) {
    try {
        // убираем пробелы, если они есть
        val clean = this.replace(" ", "").trim()

        require(clean.length >= 18) { "Некорректная длина штрих-кода: ${clean.length}" }

        val barcodeType = BarcodeIdType.entries.firstOrNull { it.type == clean.substring(0, 2).toInt() }


        // индексы 2..6 (5 символов) — артикул
        val articleRaw = clean.substring(2, 7)
        val article = articleRaw.trimStart('0').ifEmpty { "0" }.toInt()

        // индексы 7..11 (5 символов) — вес в граммах
        val weightGrams = clean.substring(7, 12).toInt()
        val weightKg = weightGrams /// 1000.0
        logPrint("weightGrams: $weightGrams")
        logPrint("weightKg: $weightKg")
        // индексы 12..17 (6 символов) — дата ггммдд -> дд.мм.гг
        val dateRaw = clean.substring(12, 18)
        val day = dateRaw.substring(0, 2)
        val month = dateRaw.substring(2, 4)
        val year = dateRaw.substring(4, 6)
        val date = "$day.$month.$year"
         onSuccess(
             BarcodeData(
                 barIdCodeType = barcodeType,
                 article = article,
                 weightKg = weightKg,
                 date = date
             )
         )
    }catch (e: Exception){
        onFailure(e.message.toString())

    }

//    return BarcodeData(barcodeType, article, weightKg, date)
}

fun getCurrentDateFormatted(): String {
    val sdf = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
    return sdf.format(Date())
}

fun String.parseDatesToList(): List<String>? {
    val list = this
    val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    return if (list.isNotEmpty()) {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(list, type)
    } else listOf()
}

fun parseDatesToString(items: List<String>): String {
    val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    return gson.toJson(items)
}





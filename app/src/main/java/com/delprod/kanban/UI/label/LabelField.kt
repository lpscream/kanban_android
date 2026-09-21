package com.delprod.kanban.UI.label

import com.delprod.kanban.db.LabelData
import com.google.zxing.BarcodeFormat
import org.apache.poi.sl.usermodel.TextParagraph

data class LabelField(
    var uniqueName: String,              // техническое название (dstu, name, address)
    var label: String,                   // название поля для UI (не печатается) (Наименование, ДСТУ, Адрес производства, Пищевая ценность)
    var uuid: String = "",               //
    var text: String,                    // текст для печати на этикетке
    var xMm: Float,                      // координата Х
    var yMm: Float,                      // координата У
    var widthMm: Float,                  // ширина
    var heightMm: Float,                 // высота
    var labelName: String = "",          // общее название этикетки
    var code: String = "99999",          // технический идентификатор, артикул 1c
    var fontSizeMm: Float = 3.0f,        // размер шрифта
    var bold: Boolean = false,           // жирный текст
    val id: Int = 0,                      // table primary key
    var align: TextParagraph.TextAlign = TextParagraph.TextAlign.LEFT,
    var barCodeFormat: BarcodeFormat = BarcodeFormat.CODE_128
)


fun labelFieldMapper(items: List<LabelField>): List<LabelData>{
    val labels = items.map {
        LabelData(
            id = it.id,
            uniqueName = it.uniqueName,
            code = it.code,
            uuid = it.uuid,
            label = it.label,
            text = it.text,
            xMm = it.xMm,
            yMm = it.yMm,
            widthMm = it.widthMm,
            heightMm = it.heightMm,
            labelName = it.labelName,
            fontSizeMm = it.fontSizeMm,
            bold = it.bold,
            align = it.align.name,
            barCodeFormat = it.barCodeFormat.name,
        )
    }
    return labels
}

//fun labelDataMappeer(items: List<LabelData>): List<LabelField>{
//    return items.map {
//        LabelField(
//            uniqueName = it.uniqueName,
//            label = it.label,
//            uuid = it.uuid,
//            text = it.text,
//            xMm = it.xMm,
//            yMm = it.yMm,
//            widthMm = it.widthMm,
//            heightMm = it.heightMm,
//            labelName = it.labelName,
//            code = it.code,
//            fontSizeMm = it.fontSizeMm,
//            bold = it.bold,
//            id = it.id,
//            align = it.align.toString(),
//            barCodeFormat = it.barCodeFormat
//        )
//    }
//}


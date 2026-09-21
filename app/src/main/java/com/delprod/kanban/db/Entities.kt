package com.delprod.kanban.db

import com.google.zxing.BarcodeFormat
import kotlinx.serialization.Serializable
import org.apache.poi.sl.usermodel.TextParagraph

/**
 * These used to be Room @Entity classes backed by a local SQLite database. The database now
 * lives on the backend server (see /backend), reached over the REST API in
 * [com.delprod.kanban.data.api.KanbanApiService]; these remain as the JSON-serializable shapes
 * shared between requests/responses and the rest of the app's UI code, which is why field names
 * still mirror the server's DTOs.
 */

//заголовок/данные покупателя, водителя, заказа
@Serializable
data class SubdivisionEntity(
    val id: Long = 0,
    val code: String,
    val fullLabel: String,
    val region: String,
    val responsiblePerson: String,
    val phone: String,
    val unitId: String,
    val category: String
)

//номенклатура
@Serializable
data class NomenclatureEntity(
    var id: Long = 0,
    val code: String,
    val name: String,
    val unit: String,
    val plu: String?
)

@Serializable
data class OrderLineEntity(
    val id: Long = 0,
    val dateEpochDay: String,       // LocalDate.toEpochDay()
    val subdivisionId: Long,
    val nomenclatureId: Long,
    val quantityOrdered: Int?,
    val quantityExecuted: Int?,
    val boxSum: Int?,
    val dateList: String?,
    val sourceFileName: String?,
    val importedAtMillis: Long?,
    val uuid: String?
)

@Serializable
data class PrinterSettings(
    val id: Long = 0,
    val name: String,
    val ip: String,
    val port: String,
    val protocol: String = "TSPL"
)

@Serializable
data class LabelData(
    var id: Int = 0,
    var uniqueName: String = "",
    var code: String = "",
    var uuid: String = "",
    var label: String = "",       // название поля для UI (не печатается)
    var text: String = "",        //текст для печати на этикетке
    var xMm: Float = 0f,
    var yMm: Float = 0f,
    var widthMm: Float = 0f,
    var heightMm: Float = 0f,
    var labelName: String = "",
    var fontSizeMm: Float = 3f,
    var bold: Boolean = false,
    var align: String = TextParagraph.TextAlign.LEFT.name,
    var barCodeFormat: String = BarcodeFormat.CODE_128.name
)

/** Server-side join of order line + subdivision + nomenclature. */
@Serializable
data class OrderLineWithDetails(
    val id: Long,
    val dateEpochDay: String,
    val quantityOrdered: Int?,
    val quantityExecuted: Int?,
    val subdivisionId: Long,
    val subdivisionCode: String,
    val subdivisionRegion: String,
    val boxSum: Int?,
    val dateList: String?,
    val fullLabel: String,
    val nomenclatureId: String,
    val nomenclatureName: String,
    val nomenclatureUnit: String,
    val nomenclatureCode: String,
    val nomenclaturePlu: String,
    val uuid: String
)


data class OrderList(
    val uuid: String,
    val subdivisionCode: String,
    val dateEpochDay: String,
)


@Serializable
data class ExportData(
    val subdivision: List<SubdivisionEntity>,
    val nomenclature: List<NomenclatureEntity>,
    val orders: List<OrderLineEntity>,
    val printer: List<PrinterSettings>,
    val label: List<LabelData>
)

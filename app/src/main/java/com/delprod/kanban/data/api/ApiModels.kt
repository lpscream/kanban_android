package com.delprod.kanban.data.api

import com.delprod.kanban.data.NomenclatureItem
import com.delprod.kanban.data.Subdivision

/** Request/response envelopes for endpoints that don't map 1:1 onto an existing entity shape. */

data class NomenclatureUpdateRequest(val plu: String, val name: String)

data class ImportLineRequest(
    val dateEpochDay: String,
    val subdivision: Subdivision,
    val nomenclature: NomenclatureItem,
    val quantityOrdered: Int?,
    val quantityExecuted: Int?,
    val uuid: String
)

data class ImportRequest(val sourceFileName: String, val lines: List<ImportLineRequest>)

data class ImportResult(val importedLines: Int, val replacedDates: List<String>)

data class OrderLineUpdateRequest(val boxSum: Int, val dateList: String, val quantityExecuted: Int)

data class LabelGroupSaved(val uuid: String)

data class DeleteByIdsRequest(val ids: List<Int>)

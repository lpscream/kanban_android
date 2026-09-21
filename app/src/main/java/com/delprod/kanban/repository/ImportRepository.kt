package com.delprod.kanban.repository

import com.delprod.kanban.core.logPrint
import com.delprod.kanban.data.FoodOrderReport
import com.delprod.kanban.data.api.DeleteByIdsRequest
import com.delprod.kanban.data.api.ImportLineRequest
import com.delprod.kanban.data.api.ImportRequest
import com.delprod.kanban.data.api.KanbanApiService
import com.delprod.kanban.data.api.NomenclatureUpdateRequest
import com.delprod.kanban.data.api.OrderLineUpdateRequest
import com.delprod.kanban.db.ExportData
import com.delprod.kanban.db.LabelData
import com.delprod.kanban.db.NomenclatureEntity
import com.delprod.kanban.db.OrderLineEntity
import com.delprod.kanban.db.PrinterSettings
import com.delprod.kanban.db.SubdivisionEntity
import com.google.gson.Gson
import java.time.LocalDate

/**
 * Talks to the backend REST API (see /backend) instead of a local Room database — all business
 * data now lives on the server, reachable only once logged in (see [com.delprod.kanban.data.auth]).
 */
class ImportRepository(private val api: KanbanApiService, private val gson: Gson) {

    suspend fun fetchPrinterList() = api.fetchPrinters()

    suspend fun fetchLabelNames() = api.fetchAllLabels()

    suspend fun fetchLabel(uuid: String) = api.fetchLabel(uuid.trim())

    suspend fun deleteLabel(uuid: String) = api.deleteLabel(uuid.trim())

    suspend fun deleteLabelById(ids: List<Int>) = api.deleteLabelsByIds(DeleteByIdsRequest(ids))

    suspend fun insertLabel(label: List<LabelData>) {
        api.saveNewLabelGroup(label)
    }

    suspend fun addOnBasicLabel(label: LabelData) {
        api.duplicateLabelGroup(label.uuid)
    }

    suspend fun updateLabel(label: List<LabelData>) = api.updateLabels(label)

    suspend fun insertPrinter(printer: PrinterSettings) = api.createPrinter(printer)

    suspend fun updatePrinter(printer: PrinterSettings) = api.updatePrinter(printer.id, printer)

    suspend fun deletePrinter(printer: PrinterSettings) = api.deletePrinter(printer.id)

    suspend fun deleteOrderByUuid(uuid: String) = api.deleteOrderByUuid(uuid)

    suspend fun getProductsForOrder(uuid: String) = api.fetchProductsForOrder(uuid)

    suspend fun updateOrderLineWithBoxsumQuantityexecuted(
        id: Long,
        uuid: String,
        boxSum: Int,
        date: String,
        quantityExecuted: Int
    ) = api.updateOrderLine(id, uuid, OrderLineUpdateRequest(boxSum, date, quantityExecuted))

    suspend fun getSubDivisionsList() = api.fetchOrdersList()

    suspend fun deleteItemFromOrder(id: Long, uuid: String) = api.deleteOrderLine(id, uuid)

    suspend fun cleanExecutedWeight(id: Long, uuid: String) = api.cleanExecutedWeight(id, uuid)

    suspend fun selectAllNomenclature() = api.fetchNomenclature()

    suspend fun updateNomenclatureItem(id: Long, item: NomenclatureEntity) {
        logPrint("updateNomenclatureItem: $item")
        api.updateNomenclature(id, NomenclatureUpdateRequest(item.plu.orEmpty(), item.name))
    }

    suspend fun insertNomenclatureItem(code: String, name: String): List<NomenclatureEntity> {
        api.createNomenclature(NomenclatureEntity(code = code, name = name, unit = "кг", plu = ""))
        return api.fetchNomenclature()
    }

    suspend fun insertOrder(order: OrderLineEntity) = api.createOrderLine(order)

    /**
     * Uploads the lines whose dates are in [selectedDates] to the backend, which replaces order
     * lines for exactly those dates (see backend/README.md) — unlike the old local Room-backed
     * version, importing one date no longer wipes every previously imported date.
     */
    suspend fun importSelectedDates(report: FoodOrderReport, selectedDates: Set<LocalDate>) {
        val linesToImport = report.filterByDates(selectedDates)
        if (linesToImport.isEmpty()) return

        val request = ImportRequest(
            sourceFileName = report.sourceFileName,
            lines = linesToImport.map { line ->
                ImportLineRequest(
                    dateEpochDay = line.date.toString(),
                    subdivision = line.subdivision,
                    nomenclature = line.nomenclature,
                    quantityOrdered = line.quantityOrdered,
                    quantityExecuted = line.quantityExecuted,
                    uuid = line.uuid
                )
            }
        )
        api.importOrderLines(request)
    }

    suspend fun insertOrderLines(items: List<OrderLineEntity>) = api.createOrderLines(items)

    suspend fun insertClinet(client: SubdivisionEntity) = api.createSubdivision(client)

    suspend fun deleteClient(client: SubdivisionEntity) = api.deleteSubdivision(client.id)

    suspend fun exportDataBase(): String = gson.toJson(api.exportDatabase())

    suspend fun importAll(data: ExportData?) {
        if (data != null) api.importDatabase(data)
    }

    suspend fun fetchAllSubdivisions() = api.fetchSubdivisions()

    suspend fun fetchAllNomenclature() = api.fetchNomenclature()

    suspend fun fetchAllOrders() = api.fetchOrderLines()

    suspend fun fetchAllPrinters() = api.fetchPrinters()

    suspend fun fetchAllLabelData() = api.fetchAllLabels()
}

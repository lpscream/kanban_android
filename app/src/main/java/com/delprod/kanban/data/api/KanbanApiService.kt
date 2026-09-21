package com.delprod.kanban.data.api

import com.delprod.kanban.db.ExportData
import com.delprod.kanban.db.LabelData
import com.delprod.kanban.db.NomenclatureEntity
import com.delprod.kanban.db.OrderList
import com.delprod.kanban.db.OrderLineEntity
import com.delprod.kanban.db.OrderLineWithDetails
import com.delprod.kanban.db.PrinterSettings
import com.delprod.kanban.db.SubdivisionEntity
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** REST client for the /backend Ktor service — see backend/README.md for the full contract. */
interface KanbanApiService {

    // Subdivisions
    @GET("api/subdivisions")
    suspend fun fetchSubdivisions(): List<SubdivisionEntity>

    @POST("api/subdivisions")
    suspend fun createSubdivision(@Body subdivision: SubdivisionEntity): SubdivisionEntity

    @DELETE("api/subdivisions/{id}")
    suspend fun deleteSubdivision(@Path("id") id: Long)

    // Nomenclature
    @GET("api/nomenclature")
    suspend fun fetchNomenclature(): List<NomenclatureEntity>

    @POST("api/nomenclature")
    suspend fun createNomenclature(@Body nomenclature: NomenclatureEntity): NomenclatureEntity

    @PUT("api/nomenclature/{id}")
    suspend fun updateNomenclature(@Path("id") id: Long, @Body request: NomenclatureUpdateRequest)

    // Order lines
    @GET("api/order-lines")
    suspend fun fetchOrderLines(): List<OrderLineEntity>

    @GET("api/order-lines/details")
    suspend fun fetchOrderLinesWithDetails(): List<OrderLineWithDetails>

    @GET("api/order-lines/dates")
    suspend fun fetchImportedDates(): List<String>

    @GET("api/order-lines/subdivisions-list")
    suspend fun fetchOrdersList(): List<OrderList>

    @GET("api/order-lines/by-order/{uuid}")
    suspend fun fetchProductsForOrder(@Path("uuid") uuid: String): List<OrderLineWithDetails>

    @POST("api/order-lines")
    suspend fun createOrderLine(@Body order: OrderLineEntity): OrderLineEntity

    @POST("api/order-lines/batch")
    suspend fun createOrderLines(@Body orders: List<OrderLineEntity>)

    @POST("api/order-lines/import")
    suspend fun importOrderLines(@Body request: ImportRequest): ImportResult

    @PUT("api/order-lines/{id}")
    suspend fun updateOrderLine(
        @Path("id") id: Long,
        @Query("uuid") uuid: String,
        @Body request: OrderLineUpdateRequest
    )

    @PUT("api/order-lines/{id}/clean")
    suspend fun cleanExecutedWeight(@Path("id") id: Long, @Query("uuid") uuid: String)

    @DELETE("api/order-lines/{id}")
    suspend fun deleteOrderLine(@Path("id") id: Long, @Query("uuid") uuid: String)

    @DELETE("api/order-lines/by-order/{uuid}")
    suspend fun deleteOrderByUuid(@Path("uuid") uuid: String)

    // Printers
    @GET("api/printers")
    suspend fun fetchPrinters(): List<PrinterSettings>

    @POST("api/printers")
    suspend fun createPrinter(@Body printer: PrinterSettings): PrinterSettings

    @PUT("api/printers/{id}")
    suspend fun updatePrinter(@Path("id") id: Long, @Body printer: PrinterSettings)

    @DELETE("api/printers/{id}")
    suspend fun deletePrinter(@Path("id") id: Long)

    // Labels
    @GET("api/labels")
    suspend fun fetchAllLabels(): List<LabelData>

    @GET("api/labels/{uuid}")
    suspend fun fetchLabel(@Path("uuid") uuid: String): List<LabelData>

    @POST("api/labels")
    suspend fun saveNewLabelGroup(@Body fields: List<LabelData>): LabelGroupSaved

    @POST("api/labels/{uuid}/duplicate")
    suspend fun duplicateLabelGroup(@Path("uuid") uuid: String): LabelGroupSaved

    @PUT("api/labels")
    suspend fun updateLabels(@Body labels: List<LabelData>)

    @DELETE("api/labels/{uuid}")
    suspend fun deleteLabel(@Path("uuid") uuid: String)

    @POST("api/labels/delete-by-ids")
    suspend fun deleteLabelsByIds(@Body request: DeleteByIdsRequest)

    // Full export/import
    @GET("api/export")
    suspend fun exportDatabase(): ExportData

    @POST("api/import-all")
    suspend fun importDatabase(@Body data: ExportData)
}

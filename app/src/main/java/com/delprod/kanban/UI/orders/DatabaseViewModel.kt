package com.delprod.kanban.UI.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delprod.kanban.core.logPrint
import com.delprod.kanban.core.toUserMessage
import com.delprod.kanban.data.Settings
import com.delprod.kanban.data.goods.BarcodeData
import com.delprod.kanban.data.goods.BarcodeIdType
import com.delprod.kanban.db.NomenclatureEntity
import com.delprod.kanban.db.OrderLineEntity
import com.delprod.kanban.db.OrderLineWithDetails
import com.delprod.kanban.db.OrderList
import com.delprod.kanban.repository.ImportRepository
import com.delprod.kanban.utils.parseDatesToList
import com.delprod.kanban.utils.parseDatesToString
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.any
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.log

class DatabaseViewModel(private val repository: ImportRepository, private val settings: Settings) :
    ViewModel() {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private val _uiOrderListState = MutableStateFlow<OrderListState>(OrderListState.Idle)
    val uiOrderListSate: StateFlow<OrderListState> = _uiOrderListState

    private val _uiProductsList = MutableStateFlow<ProductsList>(ProductsList.Idle)
    val uiProductList: StateFlow<ProductsList> = _uiProductsList

    private val _uiNomenclatureListState = MutableStateFlow<NomenclatueListState>(
        NomenclatueListState.Idle
    )
    val uiNomenclatureListState: StateFlow<NomenclatueListState> = _uiNomenclatureListState

    private var loadNomenclatureJob: Job? = null
    private var loadOrderListJob: Job? = null

    fun deleteOrderByUuid(
        uuid: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                repository.deleteOrderByUuid(uuid)
            }.onSuccess {
                onSuccess()
            }.onFailure {
                onFailure(it.toUserMessage())
            }
        }
    }

    fun loadOrderList() {
        _uiOrderListState.value = OrderListState.Idle
        loadOrderListJob = viewModelScope.launch {
            _uiOrderListState.value = OrderListState.Loading
            runCatching {
                repository.getSubDivisionsList()
            }.onSuccess {
                _uiOrderListState.value = OrderListState.Loaded(it)
            }.onFailure {
                _uiOrderListState.value = OrderListState.Error(it.toUserMessage())
            }
        }
    }

    fun fetchNomenclatureList() {
        _uiNomenclatureListState.value = NomenclatueListState.Idle
        loadNomenclatureJob = viewModelScope.launch {
            _uiNomenclatureListState.value = NomenclatueListState.Loading
            runCatching {
                logPrint("fetch nomenclature")
                repository.selectAllNomenclature()
            }.onSuccess {
                logPrint("nomenclature fetched")
                _uiNomenclatureListState.value = NomenclatueListState.Loaded(it)
            }.onFailure {
                _uiNomenclatureListState.value = NomenclatueListState.Error(it.toUserMessage())
            }
        }
    }

    fun resetNomenclatureList() {
        loadNomenclatureJob?.cancel()
        _uiNomenclatureListState.value = NomenclatueListState.Loaded(listOf())
        _uiNomenclatureListState.value = NomenclatueListState.Idle
    }

    fun resetOrderList() {
        loadOrderListJob?.cancel()
        _uiOrderListState.value = OrderListState.Loaded(listOf())
        _uiOrderListState.value = OrderListState.Idle
    }


    fun fetchProductList(uuid: String) {
        _uiOrderListState.value = OrderListState.Idle
        viewModelScope.launch {
            _uiProductsList.value = ProductsList.Loading
            runCatching {
                repository.getProductsForOrder(uuid)
            }.onSuccess {
                logPrint("is empty: ${it.isEmpty()}")
                if (it.isEmpty()){
                    _uiProductsList.value = ProductsList.Empty
                }else{
                    _uiProductsList.value = ProductsList.Loaded(it)
                }
                logPrint(gson.toJson(it))
                logPrint(it.toString())
            }.onFailure {
                logPrint("error fetching product list: ${it.message}")
                _uiProductsList.value = ProductsList.Error(it.toUserMessage())
            }
        }
    }


    fun insertOrder(
        order: OrderLineEntity,
        onSuccses: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                repository.insertOrder(order)
            }.onSuccess {
                onSuccses()
            }.onFailure {
                onFailure(it.toUserMessage())
            }
        }
    }

    fun deleteItemFromOrder(
        id: Long,
        uuid: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit){
        viewModelScope.launch {
            runCatching {
                repository.deleteItemFromOrder(id, uuid)
            }.onSuccess {
                onSuccess()
            }.onFailure {
                onFailure(it.toUserMessage())
            }
        }
    }

    fun cleanExecutedWeight(
        id: Long,
        uuid: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit){
        viewModelScope.launch {
            runCatching {
                repository.cleanExecutedWeight(id, uuid)
            }.onSuccess {
                onSuccess()
            }.onFailure {
                onFailure(it.toUserMessage())
            }
        }
    }

    fun plusItem(
        barcodeData: BarcodeData,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
        onPrintLastScannedItem: (OrderLineWithDetails, BarcodeData) -> Unit
    ) {
        val item = parseBarcodeWithItem(barcodeData)
        logPrint("plusItem: $item")
        if (item != null) {
            onPrintLastScannedItem(item, barcodeData)
            viewModelScope.launch {
                runCatching {
                    repository.updateOrderLineWithBoxsumQuantityexecuted(
                        id = item.id,
                        uuid = item.uuid,
                        boxSum = item.boxSum?.toInt() ?: 0,
                        date = item.dateList ?: "",
                        quantityExecuted = item.quantityExecuted ?: 0,
                    )
                }.onSuccess {
                    onSuccess(item.uuid)
                }.onFailure {
                    logPrint(it.toString())
                    onFailure(it.toUserMessage())
                }
            }
        }else{onFailure("Такого товара не существует")}
    }

    private fun parseBarcodeWithItem(barcodeData: BarcodeData): OrderLineWithDetails? {
        val currentItems = _uiProductsList.value
        if (currentItems is ProductsList.Loaded) {
            when (BarcodeIdType.valueOf(barcodeData.barIdCodeType.toString())) {
                BarcodeIdType.PLU -> {
                    val item = currentItems.listItems.find {
                        it.nomenclatureCode == barcodeData.article.toString()
                    }
                    logPrint("PLU: ${item.toString()}")
                    return plusDeatils(item, barcodeData)
                }

                BarcodeIdType.ARTICLE -> {
                    val item = currentItems.listItems.find {
                        it.nomenclatureCode == barcodeData.article.toString()
                    }
                    logPrint("ARTICLE: ${item.toString()}")
                    return plusDeatils(item, barcodeData)
                }

                BarcodeIdType.UNKNOWN -> {
                    return null
                }
            }
        } else {
            return null
        }
    }

    private fun plusDeatils(
        item: OrderLineWithDetails?,
        barcodeData: BarcodeData
    ): OrderLineWithDetails? {
        if (item != null) {
            val weight = item.quantityExecuted?.plus(barcodeData.weightKg) ?: barcodeData.weightKg
            val dateList: MutableList<String> = (item.dateList?.parseDatesToList() ?: mutableListOf("")).toMutableList()
//            val boxes = item.boxSum?.toInt()?.plus(1) ?: 1
            val boxes = item.boxSum.let { it?.toInt() ?: 0 }.plus(1)
            dateList.add(barcodeData.date)
            return OrderLineWithDetails(
                id = item.id,
                dateEpochDay = item.dateEpochDay,
                quantityOrdered = item.quantityOrdered,
                quantityExecuted = weight,
                subdivisionId = item.subdivisionId,
                subdivisionCode = item.subdivisionCode,
                subdivisionRegion = item.subdivisionRegion,
                boxSum = boxes,
                dateList = parseDatesToString(dateList),
                fullLabel = item.fullLabel,
                nomenclatureId = item.nomenclatureId,
                nomenclatureName = item.nomenclatureName,
                nomenclatureUnit = item.nomenclatureUnit,
                nomenclatureCode = item.nomenclatureCode,
                nomenclaturePlu = item.nomenclaturePlu,
                uuid = item.uuid
            )
        } else return null
    }
}


sealed interface ProductsList {
    data object Idle : ProductsList
    data object Loading : ProductsList
    data class Loaded(
        val listItems: List<OrderLineWithDetails>
    ) : ProductsList
    data object Empty: ProductsList

    data class Error(val message: String) : ProductsList
}


sealed interface OrderListState {
    data object Idle : OrderListState
    data object Loading : OrderListState
    data class Loaded(
        val listItems: List<OrderList>
    ) : OrderListState

    data class Error(val message: String) : OrderListState
}


sealed interface NomenclatueListState {
    data object Idle : NomenclatueListState
    data object Loading : NomenclatueListState
    data class Loaded(
        val listItems: List<NomenclatureEntity>
    ) : NomenclatueListState

    data class Error(val message: String) : NomenclatueListState
}
package com.delprod.kanban.UI.placing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delprod.kanban.data.models.ProductListItem
import com.delprod.kanban.db.OrderLineEntity
import com.delprod.kanban.db.SubdivisionEntity
import com.delprod.kanban.repository.ImportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlaicingOrderViewModel(private val repository: ImportRepository): ViewModel() {

    private val _uiProductListUiState = MutableStateFlow<ProductsListUiState>(ProductsListUiState.Idle)
    val uiProductListUiState: StateFlow<ProductsListUiState> = _uiProductListUiState

    private val _uiClientsListUiState = MutableStateFlow<ClientsListUiState>(ClientsListUiState.Idle)
    val uiClientsListUiState: StateFlow<ClientsListUiState> = _uiClientsListUiState


    fun fetchNomenclature(){
        viewModelScope.launch {
            _uiProductListUiState.value = ProductsListUiState.Loading
            runCatching {
                repository.fetchAllNomenclature()
            }.onSuccess {
                _uiProductListUiState.value = ProductsListUiState.Loaded(it.map {
                    ProductListItem(
                        id = it.id,
                        code = it.code,
                        name = it.name,
                        unit = it.unit,
                        plu = it.plu,
                        quantityOrdered = "",
                    )
                })
            }.onFailure {
                _uiProductListUiState.value = ProductsListUiState.Error(it.message.toString())
            }
        }
    }


    fun fetchClientList(){
        viewModelScope.launch {
            _uiClientsListUiState.value = ClientsListUiState.Loading
            runCatching {
                repository.fetchAllSubdivisions()
            }.onSuccess {
                _uiClientsListUiState.value = ClientsListUiState.Loaded(it)
            }.onFailure {
                _uiClientsListUiState.value = ClientsListUiState.Error(it.message.toString())
            }
        }
    }

    fun placeOrder(items: List<OrderLineEntity>,
                   onSuccess: () -> Unit,
                   onFailure: (String) -> Unit){
        viewModelScope.launch {
            runCatching {
                repository.insertOrderLines(items)
            }.onSuccess {
                onSuccess()
            }.onFailure {
                onFailure(it.message.toString())
            }
        }
    }
}


sealed interface ProductsListUiState{
    data object Idle: ProductsListUiState
    data object Loading: ProductsListUiState
    data class Loaded(val itemList: List<ProductListItem>): ProductsListUiState
    data class Error(val message: String): ProductsListUiState
}

sealed interface ClientsListUiState{
    data object Idle: ClientsListUiState
    data object Loading: ClientsListUiState
    data class Loaded(val itemlList: List<SubdivisionEntity>): ClientsListUiState
    data class Error(val message: String): ClientsListUiState
}
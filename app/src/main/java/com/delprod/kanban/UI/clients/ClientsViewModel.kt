package com.delprod.kanban.UI.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delprod.kanban.db.SubdivisionEntity
import com.delprod.kanban.repository.ImportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClientsViewModel(private val repository: ImportRepository): ViewModel() {

    private val _uiClientListStateFlow = MutableStateFlow<ClientListUiState>(ClientListUiState.Idle)
    val uiClintListState: StateFlow<ClientListUiState> = _uiClientListStateFlow





    fun fetchClientList(){
        _uiClientListStateFlow.value = ClientListUiState.Idle
        viewModelScope.launch {
            _uiClientListStateFlow.value = ClientListUiState.Loading
            runCatching {
                repository.fetchAllSubdivisions()
            }.onSuccess {
                _uiClientListStateFlow.value = ClientListUiState.Loaded(it)
            }.onFailure {
                _uiClientListStateFlow.value = ClientListUiState.Error(it.message.toString())
            }
        }
    }

    fun insertClient(
        client: SubdivisionEntity,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ){
        viewModelScope.launch {
            runCatching {
                repository.insertClinet(client)
            }.onSuccess {
                onSuccess()
            }.onFailure {
                onFailure(it.message.toString())
            }
        }
    }


    fun deleteClient(
        client: SubdivisionEntity,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ){
        viewModelScope.launch {
            runCatching {
                repository.deleteClient(client)
            }.onSuccess {
                onSuccess()
            }.onFailure {
                onFailure(it.message.toString())
            }
        }
    }

}

sealed interface ClientListUiState{
    data object Idle : ClientListUiState
    data object Loading : ClientListUiState
    data class Loaded(
        val clients: List<SubdivisionEntity>
    ) : ClientListUiState
    data class Error(val message: String) : ClientListUiState
}

package com.delprod.kanban.UI.label

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delprod.kanban.db.LabelData
import com.delprod.kanban.db.PrinterSettings
import com.delprod.kanban.repository.ImportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LabelViewModel(private val repository: ImportRepository): ViewModel() {


    private val _uiLabelNamesState = MutableStateFlow<LabelListUiState>(LabelListUiState.Idle)
    val uiLabelNamesState: StateFlow<LabelListUiState> = _uiLabelNamesState

    private val _uiPrinterNamesState = MutableStateFlow<PrinterListUiState>(PrinterListUiState.Idle)
    val uiPrinterNamesState: StateFlow<PrinterListUiState> = _uiPrinterNamesState

    private val _uiLabelState = MutableStateFlow<LabelUiState>(LabelUiState.Idle)
    val uiLabelState: StateFlow<LabelUiState> = _uiLabelState


    fun insertLabel(label: List<LabelData>,
                    onSuccess: ()  -> Unit,
                    onFailure: (String) -> Unit){
        viewModelScope.launch {
            runCatching {
                repository.insertLabel(label)
            }.onSuccess {
                onSuccess()
            }.onFailure {
                onFailure(it.message.toString())
            }
        }
    }

    fun updateLabel(label: List<LabelData>,
                    onSuccess: ()  -> Unit,
                    onFailure: (String) -> Unit){
        viewModelScope.launch {
            runCatching {
                repository.updateLabel(label)
            }.onSuccess {
                onSuccess()
            }.onFailure {
                onFailure(it.message.toString())
            }
        }
    }

    fun insertPrinter(printer: PrinterSettings){
        viewModelScope.launch {
            runCatching {
                repository.insertPrinter(printer)
            }.onSuccess {
                fetchPtrinterListNames()
            }
        }
    }

    fun updatePrinter(printer: PrinterSettings){
        viewModelScope.launch {
            runCatching {
                repository.updatePrinter(printer)
            }.onSuccess {
                fetchPtrinterListNames()
            }
        }
    }

    fun deletePrinter(printer: PrinterSettings){
        viewModelScope.launch {
            runCatching {
                repository.deletePrinter(printer)
            }.onSuccess {
                fetchPtrinterListNames()
            }
        }

    }

    fun fetchLabelListNames(){
        viewModelScope.launch {
            _uiLabelNamesState.value = LabelListUiState.Idle
            _uiLabelNamesState.value = LabelListUiState.Loading
            runCatching {
                repository.fetchLabelNames()
            }.onSuccess {
                _uiLabelNamesState.value = LabelListUiState.Loaded(it)
            }.onFailure {
                _uiLabelNamesState.value = LabelListUiState.Error(it.message!!)
            }
        }
    }


    fun fetchLabel(uuid: String){
        viewModelScope.launch {
            _uiLabelState.value = LabelUiState.Idle
            _uiLabelState.value = LabelUiState.Loading
            runCatching {
                repository.fetchLabel(uuid)
            }.onSuccess {
                _uiLabelState.value = LabelUiState.Loaded(it)
            }.onFailure {
                _uiLabelState.value = LabelUiState.Error(it.message.toString())
            }
        }
    }

    fun fetchLabelForEdit(uuid: String,
                          onSuccess: (List<LabelData>) -> Unit,
                          onFailure: (String) -> Unit){
        viewModelScope.launch {
            runCatching {
                repository.fetchLabel(uuid)
            }.onSuccess {
                onSuccess(it)
            }.onFailure {
                onFailure(it.message.toString())
            }
        }
    }


    fun deleteLabel(ids: List<Int>){
        viewModelScope.launch {
            runCatching {
                repository.deleteLabelById(ids)
            }.onSuccess {
                _uiLabelState.value = LabelUiState.Deleted
            }.onFailure {
                _uiLabelState.value = LabelUiState.Error(it.message.toString())
            }
        }
    }


    fun fetchPtrinterListNames(){
        viewModelScope.launch {
            _uiPrinterNamesState.value = PrinterListUiState.Idle
            _uiPrinterNamesState.value = PrinterListUiState.Loading
            runCatching {
                repository.fetchPrinterList()
            }.onSuccess {
                _uiPrinterNamesState.value = PrinterListUiState.Loaded(it)
            }.onFailure {
                _uiPrinterNamesState.value = PrinterListUiState.Error(it.message.toString())
            }
        }
    }


    fun addOnBasicLabel(
        label: LabelData,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit){
        viewModelScope.launch {
            runCatching {
                repository.addOnBasicLabel(label)
            }.onSuccess {
                onSuccess()
            }.onFailure {
                onFailure(it.message.toString())
            }
        }
    }
}


sealed interface LabelListUiState {
    data object Idle : LabelListUiState
    data object Loading : LabelListUiState
    data class Loaded(
        val items: List<LabelData>
    ) : LabelListUiState
    data class Error(val message: String) : LabelListUiState
}


sealed interface LabelUiState{
    data object Idle : LabelUiState
    data object Loading : LabelUiState
    data class Loaded(
        val label: List<LabelData>
    ) : LabelUiState
    data class Error(val message: String) : LabelUiState
    data object Deleted: LabelUiState
}



sealed interface PrinterListUiState {
    data object Idle : PrinterListUiState
    data object Loading : PrinterListUiState
    data class Loaded(
        val printerNames: List<PrinterSettings>
    ): PrinterListUiState
    data class Error(val message: String): PrinterListUiState
}
package com.delprod.kanban.UI.goods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delprod.kanban.core.toUserMessage
import com.delprod.kanban.data.Settings
import com.delprod.kanban.db.NomenclatureEntity
import com.delprod.kanban.repository.ImportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NomenclatureViewModel(private val repository: ImportRepository, private val settings: Settings): ViewModel() {


    private val _uiNomenklatureListState = MutableStateFlow<NomenclatureList>(NomenclatureList.Idle)
    val uiNomenklatureListState: StateFlow<NomenclatureList> = _uiNomenklatureListState



    fun selectAllNomenclature(){
        viewModelScope.launch {
            _uiNomenklatureListState.value = NomenclatureList.Loading
            runCatching {
                repository.selectAllNomenclature()
            }.onSuccess {
                _uiNomenklatureListState.value = NomenclatureList.Loaded(it)
            }.onFailure {
                _uiNomenklatureListState.value = NomenclatureList.Error(it.toUserMessage())
            }
        }
    }

    fun updateNomeclatureItem(id: Long, item: NomenclatureEntity, onSuccess: () -> Unit, onFailure: (String) -> Unit){
        viewModelScope.launch {
            runCatching {
                repository.updateNomenclatureItem(id, item)
            }.onSuccess {
                onSuccess()
            }.onFailure {
                onFailure(it.toUserMessage())
            }
        }
    }

    fun insertNomenclature(code: String, name: String){
        viewModelScope.launch {
            _uiNomenklatureListState.value = NomenclatureList.Loading
            runCatching {
                repository.insertNomenclatureItem(code, name)
            }.onSuccess {
                _uiNomenklatureListState.value = NomenclatureList.Loaded(it)
            }.onFailure {
                _uiNomenklatureListState.value = NomenclatureList.Error(it.toUserMessage())
            }
        }
    }
}


sealed interface NomenclatureList{
    data object Idle: NomenclatureList
    data object Loading: NomenclatureList
    data class Loaded(
        val listItems: List<NomenclatureEntity>
    ): NomenclatureList
    data class Error(val message: String): NomenclatureList
}
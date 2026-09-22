package com.delprod.kanban.UI.importui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delprod.kanban.core.logPrint
import com.delprod.kanban.core.toUserMessage
import com.delprod.kanban.repository.ImportRepository
import com.delprod.kanban.data.FoodOrderReport
import com.delprod.kanban.google.GoogleSheetsLoader
import com.delprod.kanban.google.PublicLinkExcelLoader
import com.delprod.kanban.parser.FoodOrderXlsParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.time.LocalDate

sealed interface ImportUiState {
    data object Idle : ImportUiState
    data object Loading : ImportUiState
    data class Loaded(
        val report: FoodOrderReport,
        val selectedDates: Set<LocalDate>
    ) : ImportUiState
    data class Imported(val importedCount: Int) : ImportUiState
    data class Error(val message: String) : ImportUiState
}

class ImportViewModel(
    private val repository: ImportRepository,
    private val googleSheetsLoader: GoogleSheetsLoader? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState: StateFlow<ImportUiState> = _uiState

    /** Вызов после выбора файла через Storage Access Framework (ACTION_OPEN_DOCUMENT). */
    fun loadFromLocalFile(openStream: () -> InputStream, fileName: String) {
        viewModelScope.launch {
            _uiState.value = ImportUiState.Loading
            runCatching {
                withContext(Dispatchers.IO) {
                    openStream().use { FoodOrderXlsParser.parse(it, fileName) }
                }
            }.onSuccess { report ->
                _uiState.value = ImportUiState.Loaded(report, report.availableDates.toSet())
            }.onFailure { e ->
                _uiState.value = ImportUiState.Error(e.message ?: "Ошибка чтения файла")
            }
        }
    }

    /**
     * Вызов после того как пользователь выбрал Google Sheet(передакется его URL или fileId)
     * */
    fun loadFromGoogleSheet(fileIdOrUrl: String) {
        val loader = googleSheetsLoader ?: run {
            _uiState.value = ImportUiState.Error("Google Sheets интеграция не настроена")
            return
        }
        val fileId = GoogleSheetsLoader.extractFileId(fileIdOrUrl) ?: fileIdOrUrl
        viewModelScope.launch {
            _uiState.value = ImportUiState.Loading
            runCatching {
                val stream = loader.downloadAsXls(fileId)
                withContext(Dispatchers.IO) {
                    stream.use { FoodOrderXlsParser.parse(it, "google_sheet_$fileId.xls") }
                }
            }.onSuccess { report ->
                _uiState.value = ImportUiState.Loaded(report, report.availableDates.toSet())
            }.onFailure { e ->
                _uiState.value = ImportUiState.Error(e.toUserMessage())
            }
        }
    }


    /**
     * Загрузка по публичной ссылке — БЕЗ авторизации.
     * Работает для: прямых ссылок на .xls/.xlsx, публичных Google Sheets
     * и Google Drive файлов с доступом "Все, у кого есть ссылки".
     */
    fun loadFromUrl(url: String) {
        viewModelScope.launch {
            _uiState.value = ImportUiState.Loading
            runCatching {
                val stream = PublicLinkExcelLoader.download(url)
                withContext(Dispatchers.IO) {
                    stream.use { FoodOrderXlsParser.parse(it, fileNameFromUrl(url)) }
                }
            }.onSuccess { report ->
                _uiState.value = ImportUiState.Loaded(report, report.availableDates.toSet())
            }.onFailure { e ->
                _uiState.value = ImportUiState.Error(e.toUserMessage())
            }
        }
    }

    private fun fileNameFromUrl(url: String): String =
        url.substringAfterLast('/').substringBefore('?').ifBlank { "document_from_url.xlsx" }


    fun toggleDate(date: LocalDate) {
        val state = _uiState.value
        if (state !is ImportUiState.Loaded) return
        val newSelected = state.selectedDates.toMutableSet().apply {
            if (contains(date)) remove(date) else add(date)
        }
        _uiState.value = state.copy(selectedDates = newSelected)
    }

    fun selectAllDates(select: Boolean) {
        val state = _uiState.value
        if (state !is ImportUiState.Loaded) return
        _uiState.value = state.copy(
            selectedDates = if (select) state.report.availableDates.toSet() else emptySet()
        )
    }




    fun confirmImport() {
        val state = _uiState.value
        if (state !is ImportUiState.Loaded) return
        viewModelScope.launch {
            _uiState.value = ImportUiState.Loading
            runCatching {
                repository.importSelectedDates(state.report, state.selectedDates)
            }.onSuccess {
                val count = state.report.filterByDates(state.selectedDates).size
                _uiState.value = ImportUiState.Imported(count)
            }.onFailure { e ->
                _uiState.value = ImportUiState.Error(e.toUserMessage())
            }
        }
    }






    fun reset() {
        _uiState.value = ImportUiState.Idle
    }
}

package com.delprod.kanban.UI.importui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.delprod.kanban.R
import com.delprod.kanban.core.App
import com.delprod.kanban.data.Settings
import com.delprod.kanban.repository.ImportRepository
import com.delprod.kanban.databinding.ActivityImportBinding
import com.delprod.kanban.google.GoogleSheetsLoader
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch

class ImportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImportBinding
    private lateinit var viewModel: ImportViewModel
    private lateinit var repository: ImportRepository


    private lateinit var dateAdapter: DateCheckboxAdapter

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val fileName = uri.lastPathSegment ?: "document.xls"
            viewModel.loadFromLocalFile(
                openStream = { contentResolver.openInputStream(uri)!! },
                fileName = fileName
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = (this.application as App).importRepository

        // TODO: подставить реальный провайдер access token после настройки Google Sign-In
        val googleSheetsLoader: GoogleSheetsLoader? = null

        viewModel = ViewModelProvider(
            this,
            ImportViewModelFactory(repository, googleSheetsLoader)
        )[ImportViewModel::class.java]

        dateAdapter = DateCheckboxAdapter(onToggle = { date -> viewModel.toggleDate(date) })
        binding.dateRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.dateRecyclerView.adapter = dateAdapter
        binding.btnPickLocalFile.setOnClickListener {
            openDocumentLauncher.launch(
                arrayOf("application/vnd.ms-excel", "application/octet-stream")
            )
        }

        binding.btnPickGoogleSheet.setOnClickListener { showGoogleSheetUrlDialog() }
        binding.btnSelectAll.setOnClickListener { viewModel.selectAllDates(true) }
        binding.btnSelectNone.setOnClickListener { viewModel.selectAllDates(false) }
        binding.btnImport.setOnClickListener { viewModel.confirmImport() }
        observeState()
    }

    private fun showGoogleSheetUrlDialog() {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_google_sheet_url, null)
        val editText = dialogView.findViewById<EditText>(R.id.editGoogleSheetUrl)
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()

        val settings = Settings(this, gson)
        editText.setText(settings.googleSheetUrl())
        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Загрузить") { _, _ ->
                val url = editText.text.toString().trim()
                settings.googleSheetUrl(url)
                if (url.isNotEmpty()) viewModel.loadFromUrl(url)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                render(state)
            }
        }
    }

    private fun render(state: ImportUiState) {
        binding.progressBar.visibility = if (state is ImportUiState.Loading) View.VISIBLE else View.GONE

        val loaded = state as? ImportUiState.Loaded
        val showDateUi = loaded != null
        binding.btnSelectAll.visibility = if (showDateUi) View.VISIBLE else View.GONE
        binding.btnSelectNone.visibility = if (showDateUi) View.VISIBLE else View.GONE
        binding.dateRecyclerView.visibility = if (showDateUi) View.VISIBLE else View.GONE
        binding.btnImport.visibility = if (showDateUi) View.VISIBLE else View.GONE

        when (state) {
            is ImportUiState.Idle -> {
                binding.statusText.text = "Выберите файл для импорта."
            }
            is ImportUiState.Loading -> {
                binding.statusText.text = "Загрузка..."
            }
            is ImportUiState.Error -> {
                binding.statusText.text = "Ошибка: ${state.message}"
            }
            is ImportUiState.Loaded -> {
                binding.statusText.text = "Файл: ${state.report.sourceFileName}. Дат найдено: ${state.report.availableDates.size}"
                dateAdapter.submit(state.report.dateGroups, state.selectedDates)
                binding.btnImport.isEnabled = state.selectedDates.isNotEmpty()
                binding.btnImport.text = "Сохранить выбранные даты в базу (${state.selectedDates.size})"
            }
            is ImportUiState.Imported -> {
                binding.statusText.text = "Успешно импортовано ${state.importedCount} строк."
                Toast.makeText(this, "Импорт завершен", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class ImportViewModelFactory(
    private val repository: ImportRepository,
    private val googleSheetsLoader: GoogleSheetsLoader?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ImportViewModel(repository, googleSheetsLoader) as T
    }
}

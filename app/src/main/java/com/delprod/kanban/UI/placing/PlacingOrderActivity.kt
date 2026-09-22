package com.delprod.kanban.UI.placing

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.delprod.kanban.R
import com.delprod.kanban.UI.placing.adapter.PlacingProductListAdapter
import com.delprod.kanban.core.App
import com.delprod.kanban.core.logPrint
import com.delprod.kanban.databinding.ActivityPlacingOrderBinding
import com.delprod.kanban.db.OrderLineEntity
import com.delprod.kanban.db.SubdivisionEntity
import com.delprod.kanban.utils.EditTextExtentions.dateFormatValidation
import com.delprod.kanban.utils.kToGrams
import com.delprod.kanban.utils.launchAndCollectIn
import com.delprod.kanban.utils.toast
import java.util.UUID
import kotlin.uuid.Uuid

class PlacingOrderActivity : AppCompatActivity() {

    private val binding by lazy { ActivityPlacingOrderBinding.inflate(layoutInflater) }
    private lateinit var viewModel: PlaicingOrderViewModel

    private var clientEntity: SubdivisionEntity? = null

    private var dateValidation = false

    private lateinit var adapter: PlacingProductListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = (this.application as App).placingOrderViewModel

        binding.shippingDate.dateFormatValidation {
            dateValidation = it
            logPrint(it.toString())
        }

        bindViewModel()
        viewModel.fetchNomenclature()
        binding.subdivisionChooseBtn.setOnClickListener {
            viewModel.fetchClientList()
        }

        binding.saveBtn.setOnClickListener {
            val listValid = adapter.fetchItemList().any { it.quantityOrdered != "" }
            when{
                clientEntity == null -> {
                    toast(resources.getString(R.string.placing_order_client_entity_error))
                    return@setOnClickListener
                }
                binding.shippingDate.text.toString() == "" -> {
                    toast(resources.getString(R.string.placing_order_empty_date_error))
                    return@setOnClickListener
                }
                !dateValidation -> {
                    toast(resources.getString(R.string.placing_order_date_valid_error))
                    return@setOnClickListener
                }
                !listValid -> {
                    toast(resources.getString(R.string.placing_order_empty_weight_error))
                    return@setOnClickListener
                }
            }
            val time = System.currentTimeMillis()
            val uuid = UUID.randomUUID().toString()
            val filledItems = adapter.fetchItemList().filter { it.quantityOrdered != "" }
            val items = filledItems.map { OrderLineEntity(
                dateEpochDay = binding.shippingDate.text.toString(),
                subdivisionId = clientEntity!!.id,
                nomenclatureId = it.id,
                quantityOrdered = it.quantityOrdered?.kToGrams() ?: 0,
                quantityExecuted = 0,
                boxSum = 0,
                dateList = "",
                sourceFileName = "manual",
                importedAtMillis = time,
                uuid = uuid
            ) }
            viewModel.placeOrder(
                items,
                onSuccess = {
                    toast("Сохранено")
                    finish()},
                onFailure = {
                    toast(it)
                })
        }
    }

    private fun bindViewModel() {
        viewModel.uiClientsListUiState.launchAndCollectIn(this){
            when(it){
                is ClientsListUiState.Idle -> {}
                is ClientsListUiState.Loading -> {
                    binding.root.isVisible = false
                    binding.progressBar.isVisible = true
                }
                is ClientsListUiState.Loaded -> {
                    binding.root.isVisible = true
                    binding.progressBar.isVisible = false
                    if (it.itemlList.isEmpty()) {
                        toast(R.string.server_empty_response_message)
                    }
                    PlacingClientDialog.create(this@PlacingOrderActivity, it.itemlList){
                        clientEntity = it
                    }
                }
                is ClientsListUiState.Error -> {
                    toast(it.message)
                }
            }
        }

        viewModel.uiProductListUiState.launchAndCollectIn(this){
            when(it){
                is ProductsListUiState.Idle -> {}
                is ProductsListUiState.Loading -> {
                    binding.root.isVisible = false
                    binding.progressBar.isVisible = true
                }
                is ProductsListUiState.Loaded -> {
                    binding.root.isVisible = true
                    binding.progressBar.isVisible = false
                    if (it.itemList.isEmpty()) {
                        toast(R.string.server_empty_response_message)
                    }
                    adapter = PlacingProductListAdapter(it.itemList)
                    binding.productItemList.layoutManager = LinearLayoutManager(this@PlacingOrderActivity)
                    binding.productItemList.adapter = adapter
                }
                is ProductsListUiState.Error -> {
                    toast(it.message)
                }
            }
        }
    }
}
package com.delprod.kanban.UI.orders

import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.KeyEvent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.delprod.kanban.R
import com.delprod.kanban.UI.orders.dialogs.AddProductToOrderDialog
import com.delprod.kanban.UI.orders.dialogs.AddWeightDateToProduct
import com.delprod.kanban.UI.orders.dialogs.DeleteOrderDialog
import com.delprod.kanban.UI.orders.dialogs.OrdersListDialog
import com.delprod.kanban.UI.orders.dialogs.SendOrderDialog
import com.delprod.kanban.UI.settings.SettingsViewModel
import com.delprod.kanban.core.App
import com.delprod.kanban.core.logPrint
import com.delprod.kanban.data.Settings
import com.delprod.kanban.data.goods.BarcodeData
import com.delprod.kanban.data.goods.ProductModel
import com.delprod.kanban.data.goods.Order
import com.delprod.kanban.databinding.ActivityOrderBinding
import com.delprod.kanban.db.OrderLineWithDetails
import com.delprod.kanban.utils.launchAndCollectIn
import com.delprod.kanban.utils.parseBarcode
import com.delprod.kanban.utils.toast
import com.delprod.kanban.utils.weightGramsToKg
import kotlin.collections.forEachIndexed

class OrderActivity : AppCompatActivity() {
    private var isMenuOpen = false
    private val binding by lazy { ActivityOrderBinding.inflate(layoutInflater) }
    private lateinit var settings: Settings
    private lateinit var viewModelSettings: SettingsViewModel
    private lateinit var viewModel: DatabaseViewModel
    private lateinit var adapter: ProductsItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModelSettings = (this.application as App).settingsViewModel
        viewModel = (this.application as App).databaseViewModel
        settings = (this.application as App).settings
        binding.barCodeReader.showSoftInputOnFocus = false
        viewModel.resetOrderList()
        viewModel.resetNomenclatureList()

        if (settings.order() != null) {
            binding.quantitySum.setText("${settings.order()?.sumBoxQuantity} ящ.")
            binding.weightSum.setText("общ.вес: ${settings.order()!!.sumWeight.weightGramsToKg()} кг.")
        }


        binding.barCodeReader.setOnLongClickListener {
            binding.barCodeReader.showSoftInputOnFocus = !binding.barCodeReader.showSoftInputOnFocus
            return@setOnLongClickListener true
        }

        binding.currentScan.isVisible = settings.bottomCurrentStateVisibility()

        bindViewModel()
        setupSpeedDial()
    }

    private fun bindViewModel() {
        viewModel.uiOrderListSate.launchAndCollectIn(this){
            when(it){
                is OrderListState.Idle -> {
                    binding.mainViewLayout.isVisible = true
                    binding.progressBar.isVisible = false
                }
                is OrderListState.Loading -> {
                    binding.mainViewLayout.isVisible = false
                    binding.progressBar.isVisible = true
                }
                is OrderListState.Error -> {
                    binding.mainViewLayout.isVisible = true
                    binding.progressBar.isVisible = false
                    binding.currentScanName.text = it.message
                }
                is OrderListState.Loaded -> {
                    binding.mainViewLayout.isVisible = true
                    binding.progressBar.isVisible = false
                    logPrint(it.listItems.toString())
                    OrdersListDialog.create(
                        this@OrderActivity,
                        layoutInflater,
                        onItemSelected = {
                            viewModel.fetchProductList(it)
//                            viewModel.resetOrderList()
                        },
                        it.listItems
                    )
                }
            }
        }

        viewModel.uiProductList.launchAndCollectIn(this){
            when(it){
                is ProductsList.Idle -> {
                    binding.listItem.isVisible = false
                    binding.progressBar.isVisible = false
                    binding.emptyListMessage.isVisible = true
                    initGoodsAdapter(null)
                }
                is ProductsList.Loading -> {
                    binding.listItem.isVisible = false
                    binding.progressBar.isVisible = true
                    binding.emptyListMessage.isVisible = false
                }
                is ProductsList.Error -> {
                    binding.listItem.isVisible = false
                    binding.progressBar.isVisible = false
                    binding.emptyListMessage.isVisible = true
                    binding.currentScanName.text = it.message
                }
                is ProductsList.Loaded -> {
                    //init recycler view
                    initGoodsAdapter(it.listItems)
                    binding.subdivisionName.setText("${it.listItems[0].dateEpochDay}, ${it.listItems[0].subdivisionCode}")
                    binding.subdivisionName.tag = it.listItems[0].uuid
                    binding.listItem.isVisible = true
                    binding.progressBar.isVisible = false
                    binding.emptyListMessage.isVisible = false
                }

                ProductsList.Empty -> {
                    initGoodsAdapter(null)
                    binding.subdivisionName.setText("")
                    binding.subdivisionName.tag = ""
                    binding.listItem.isVisible = false
                    binding.progressBar.isVisible = false
                    binding.emptyListMessage.isVisible = true
                }
            }
        }

        viewModel.uiNomenclatureListState.launchAndCollectIn(this){
            when(it){
                is NomenclatueListState.Idle -> {}
                is NomenclatueListState.Loading -> {}
                is NomenclatueListState.Loaded -> {
                    AddProductToOrderDialog.create(
                        this@OrderActivity,
                        layoutInflater,
                        adapter.fetchItems(),
                        it.listItems){
                        // save item to DB
                        viewModel.insertOrder(
                            it,
                            onSuccses = {
                                viewModel.fetchProductList(binding.subdivisionName.tag.toString().trim())
//                                viewModel.resetNomenclatureList()
                            },
                            onFailure = {
                                logPrint(it)
                                toast(it)
//                                viewModel.resetNomenclatureList()
                            })
                    }
                }
                is NomenclatueListState.Error -> {
                    toast(it.message)
                }
            }
        }

        viewModelSettings.sendOrderFlow.launchAndCollectIn(this) {
            if (it != null) {
                when (it.ok) {
                    true -> {
                        toast(resources.getString(R.string.order_has_been_sent))
//                        showScannedItem(null, null, null)
//                        binding.subdivisionName.text = ""
//                        binding.subdivisionName.tag = ""
//                        initGoodsAdapter(null)
                    }
                    false -> {
                        toast(resources.getString(R.string.telegram_send_message_error))
                        showScannedItem(null, null, null)
                        binding.currentScanName.setText(it.toString())
                    }
                    else -> {
                        toast(resources.getString(R.string.something_went_wrong))
                        showScannedItem(null, null, null)
                        binding.currentScanName.setText(resources.getString(R.string.something_went_wrong))
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        logPrint("keyCode: $keyCode, event: ${event}")
        when {
            event?.action == KeyEvent.ACTION_DOWN && keyCode == 1010 -> {
                binding.barCodeReader.setText("")
                binding.barCodeReader.requestFocus()
                logPrint("KEYCODE_SCANNER_F: ${binding.barCodeReader.text.toString()}")
            }

            event?.action == KeyEvent.ACTION_DOWN && keyCode == 1012 -> {
                binding.barCodeReader.setText("")
                binding.barCodeReader.requestFocus()
                logPrint("KEYCODE_SCANNER_F: ${binding.barCodeReader.text.toString()}")
            }

            event?.action == KeyEvent.ACTION_DOWN && keyCode == 1011 -> {
                binding.barCodeReader.setText("")
                binding.barCodeReader.requestFocus()
                logPrint("KEYCODE_SCANNER_F: ${binding.barCodeReader.text.toString()}")
            }

            event?.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_TAB -> {
                parseStringInput()
            }

            event?.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER -> {
                parseStringInput()
            }
        }
        return super.onKeyDown(keyCode, event)
    }


    private fun parseStringInput(){
        logPrint("KEYCODE_TAB: ${binding.barCodeReader.text.toString()}")
        //parse barcode
        val barcode = binding.barCodeReader.text.toString()
        binding.barCodeReader.setText("")
        if (barcode.length == 18) {
            barcode.parseBarcode(
                onFailure = {
                    logPrint(it)
                    toast("Ошибка штрихкода: $it")
                    binding.currentScanName.setText(it)
                },
                onSuccess = {
                    logPrint("scanned item: $it")
                    if (it.weightKg == 0){
                        vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE) )
                        val item = adapter.fetchItem(it)
                        if (item == null){
                            toast(resources.getString(R.string.no_product_in_order))
                        }else{
                            AddWeightDateToProduct.create(
                                context = this,
                                item = item,
                                barcodeData = it,
                                settings = settings,
                            ){
                                plusBarcode(it)
                                viewModel.fetchProductList(
                                    binding.subdivisionName.tag.toString().trim()
                                )
                            }
                        }
                    }else{
                        plusBarcode(it)
                        vibrate()
                    }
                }
            )
        } else {
            showScannedItem(null, null, null)
            binding.currentScanName.setText("${resources.getString(R.string.barcode_length_error)}: $barcode")
            toast(resources.getString(R.string.barcode_length_error))
        }
    }


    private fun plusBarcode(barcodeData: BarcodeData){
        if (binding.subdivisionName.tag == null){
            toast(resources.getString(R.string.take_order_before_adding_product))
        }else{
            viewModel.plusItem(
                barcodeData,
                onSuccess = {
                    viewModel.fetchProductList(it)
                },
                onFailure = {
                    toast(it)
                },
                onPrintLastScannedItem = { item, BarcodeData ->
                    binding.currentScanName.text = item.nomenclatureName
                    binding.currentScanWeight.text = barcodeData.weightKg.weightGramsToKg()
                    binding.currentScanDate.text = barcodeData.date
                    binding.weightSum.text = item.quantityExecuted?.weightGramsToKg() ?: ""
                    binding.quantitySum.text = item.boxSum.toString()
                }
            )
        }
    }

    private fun showScannedItem(item: ProductModel?, data: BarcodeData?, orders: Order?) {
        if (item != null && data != null && orders != null) {
            binding.currentScanName.setText(item.name)
            binding.currentScanDate.setText("дата: ${data.date}")
            binding.currentScanWeight.setText("${data.weightKg.weightGramsToKg()} кг.")
            binding.quantitySum.setText("${orders.sumBoxQuantity} ящ.")
            binding.weightSum.setText("общ.вес: ${orders.sumWeight.weightGramsToKg()} кг.")
        } else {
            binding.currentScanName.setText("")
            binding.currentScanDate.setText("")
            binding.currentScanWeight.setText("")
            binding.quantitySum.setText("")
            binding.weightSum.setText("")
        }
    }

    private fun initGoodsAdapter(items: List<OrderLineWithDetails>?) {
        if (items == null) {
            adapter = ProductsItemsAdapter(
                listOf(),
                onClick = {
                    toast(it.toString())
                },
                onCleanExecuted = {},
                onDeleteProduct = {}
            )
            binding.listItem.layoutManager = LinearLayoutManager(this)
            binding.listItem.adapter = adapter
            binding.emptyListMessage.isVisible = true
            binding.listItem.isVisible = false
        } else {
            adapter = ProductsItemsAdapter(
                items,
                onClick = {
                    AddWeightDateToProduct.create(
                        context = this,
                        item = it,
                        onAddBtnPressed = {
                            plusBarcode(it)
                            viewModel.fetchProductList(
                                binding.subdivisionName.tag.toString().trim()
                            )
                        },
                        barcodeData = null,
                        settings = settings,
                    )
                },
                onCleanExecuted = {
                    viewModel.cleanExecutedWeight(
                        it.id, it.uuid,
                        onSuccess = {
                            viewModel.fetchProductList(binding.subdivisionName.tag.toString().trim())
                        },
                        onFailure = {toast(it)}
                    )
                },
                onDeleteProduct = {
                    viewModel.deleteItemFromOrder(
                        it.id, it.uuid,
                        onSuccess = {
                            viewModel.fetchProductList(binding.subdivisionName.tag.toString().trim())
                        },
                        onFailure = {toast(it)}
                    )
                }
            )
            binding.listItem.layoutManager = LinearLayoutManager(this)
            binding.listItem.adapter = adapter
            binding.emptyListMessage.isVisible = false
            binding.listItem.isVisible = true
        }
    }

    private fun vibrate(effect: VibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)) {
        // 1. Получаем системный сервис вибрации
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        // 2. Проверяем, есть ли вообще вибромотор на устройстве
        if (vibrator.hasVibrator()) {
            // Для Android 8.0 (API 26) и выше
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // VIBRATION_EFFECT_STANDARD - стандартная вибрация
                vibrator.vibrate(effect)
            } else {
                // Для старых версий (устаревший метод, но рабочий)
                vibrator.vibrate(500)
            }
        }
    }

    private fun setupSpeedDial() {
        val subItems = listOf(binding.item1, binding.item2, binding.item3, binding.item4)
        subItems.forEach { view ->
            view.alpha = 0f
        }

        binding.fabMain.setOnClickListener {
            isMenuOpen = !isMenuOpen
            toggleMenu(subItems, isMenuOpen)
        }

        binding.fabMain.setOnLongClickListener {
            binding.currentScan.isVisible = !binding.currentScan.isVisible
            settings.bottomCurrentStateVisibility(binding.currentScan.isVisible)
            true
        }

        binding.scrim.setOnClickListener {
            isMenuOpen = false
            toggleMenu(subItems, false)
        }

        binding.addItemFab.setOnClickListener {
//            if (adapter != null || binding.subdivisionName.text.toString() == "" || binding.subdivisionName.tag.toString() == null){
            if (binding.subdivisionName.tag != null){
                viewModel.fetchNomenclatureList()
            }else{
                toast(resources.getString(R.string.take_order_before_adding_product))
            }
            isMenuOpen = false
            toggleMenu(subItems, false)
        }
        binding.ordersFab.setOnClickListener {
            viewModel.loadOrderList()
            isMenuOpen = false
            toggleMenu(subItems, false)
        }
        binding.deleteOrderFab.setOnClickListener {
            if (binding.subdivisionName.tag != null){
                DeleteOrderDialog.create(this) {
                    viewModel.deleteOrderByUuid(
                        binding.subdivisionName.tag.toString(),
                        onSuccess = {
                            binding.subdivisionName.tag = ""
                            binding.subdivisionName.setText("")
                            showScannedItem(null, null, null)
                            initGoodsAdapter(null)
                        },
                        onFailure = {
                            toast(it)
                        }
                    )

                }
            }else{
                toast(resources.getString(R.string.take_order_before_adding_product))
            }
            isMenuOpen = false
            toggleMenu(subItems, false)
        }
        binding.shareFab.setOnClickListener {
            if (binding.subdivisionName.tag != ""){
                SendOrderDialog.create(this, viewModelSettings, adapter.fetchItems(), layoutInflater) {
                    logPrint("trying to send message")
                }
            }

            isMenuOpen = false
            toggleMenu(subItems, false)
        }
    }

    private fun toggleMenu(items: List<View>, show: Boolean) {
        binding.scrim.visibility = if (show) View.VISIBLE else View.GONE

        // Поворот главной иконки (крестик <-> плюс)
        binding.fabMain.animate()
            .rotation(if (show) 45f else 0f)
            .setDuration(200)
            .start()

        items.forEachIndexed { index, view ->
            if (show) {
                view.visibility = View.VISIBLE
                view.alpha = 0f
                view.translationY = 50f
                view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(index * 40L)
                    .setDuration(200)
                    .start()
            } else {
                view.animate()
                    .alpha(0f)
                    .translationY(50f)
                    .setDuration(150)
                    .withEndAction { view.visibility = View.INVISIBLE }
                    .start()
            }
        }
    }
}



//private fun plusItem(data: BarcodeData) {
//    val orders = settings.order()
//    val itemByArticleInDB = settings.goodsItemNomenclature(data.article)
//    if (itemByArticleInDB == null) {
//        showScannedItem(null, null, null)
//        binding.currentScanName.setText(resources.getString(R.string.article_error_in_db))
//        toast(resources.getString(R.string.article_error_in_db))
//        return
//    }
//    if (orders == null) {
//        //create new item in order if it null
//        val newOrders = Order(
//            sumBoxQuantity = 1,
//            sumWeight = data.weightKg,
//            date = getCurrentDateFormatted(),
//            goods = listOf(
//                ProductModel(
//                    article = data.article,
//                    name = itemByArticleInDB.name,
//                    weight = data.weightKg,
//                    dateList = listOf(data.date),
//                    boxQuantity = 1
//                )
//            )
//        )
//        logPrint("newOrders: $newOrders")
//        showScannedItem(itemByArticleInDB, data, newOrders)
////            initGoodsAdapter(newOrders.goods)
//        settings.order(newOrders)
//        vibrate()
//    } else {
//        //add position in existing list items order
//        val newList = if (orders.goods.any { it.article == data.article }) {
//            orders.goods.map { item ->
//                if (item.article == data.article) {
//                    item.copy(
//                        weight = item.weight + data.weightKg,
//                        dateList = if (!item.dateList.any { it == data.date }) {
//                            item.dateList.plus(data.date)
//                        } else item.dateList,
//                        boxQuantity = item.boxQuantity + 1
//                    )
//                } else {
//                    item
//                }
//            }
//        } else {
//            orders.goods.plus(
//                ProductModel(
//                    article = data.article,
//                    name = itemByArticleInDB.name,
//                    weight = data.weightKg,
//                    dateList = listOf(data.date),
//                    boxQuantity = 1
//                )
//            )
//        }
//        val updateOrders = Order(
//            sumBoxQuantity = orders.sumBoxQuantity + 1,
//            sumWeight = orders.sumWeight + data.weightKg,
//            date = orders.date,
//            goods = newList
//        )
//        logPrint("updateOrders: $updateOrders")
//        showScannedItem(itemByArticleInDB, data, updateOrders)
////            initGoodsAdapter(updateOrders.goods)
//        settings.order(updateOrders)
//        vibrate()
//    }
//    logPrint(settings.order().toString())
//}

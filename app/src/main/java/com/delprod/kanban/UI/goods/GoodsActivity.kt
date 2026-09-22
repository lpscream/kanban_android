package com.delprod.kanban.UI.goods

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delprod.kanban.R
import com.delprod.kanban.core.App
import com.delprod.kanban.core.logPrint
import com.delprod.kanban.data.Settings
import com.delprod.kanban.databinding.ActivityGoodsBinding
import com.delprod.kanban.db.NomenclatureEntity
import com.delprod.kanban.utils.launchAndCollectIn
import com.delprod.kanban.utils.toast

class GoodsActivity : AppCompatActivity() {

    private val binding by lazy { ActivityGoodsBinding.inflate(layoutInflater) }
    private lateinit var settings: Settings
    private lateinit var viewModel: NomenclatureViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = (this.application as App).nomenclatureListViewModel

        binding.addBtn.setOnClickListener {
            GoodItemCreationDialog.create(
                context = this,
                inflater = layoutInflater,
                item = null,
                onSaveButtonPressed = { item ->
                    viewModel.insertNomenclature(item.code, item.name)
            }, onUpdateItem = { item ->
                logPrint(item.toString())
            }
            )
        }
        viewModel.uiNomenklatureListState.launchAndCollectIn(this){
            when(it){
                is NomenclatureList.Idle -> {
                    binding.goodsItemRecyclerView.isVisible = false
                    binding.message.isVisible = false
                    binding.progressBar.isVisible = false
                }
                is NomenclatureList.Loading -> {
                    binding.goodsItemRecyclerView.isVisible = false
                    binding.message.isVisible = false
                    binding.progressBar.isVisible = true
                }
                is NomenclatureList.Error -> {
                    binding.goodsItemRecyclerView.isVisible = false
                    binding.message.isVisible = true
                    binding.message.setText(it.toString())
                    binding.progressBar.isVisible = false
                }
                is NomenclatureList.Loaded -> {
                    //create ad
                    binding.goodsItemRecyclerView.isVisible = true
                    binding.message.isVisible = false
                    binding.progressBar.isVisible = false
                    if (it.listItems.isEmpty()) {
                        toast(R.string.server_empty_response_message)
                    }
                    initAdapter(it.listItems)
                }
            }
        }
        viewModel.selectAllNomenclature()
    }

    private fun initAdapter(itemList: List<NomenclatureEntity> ){
        val adapter = GoodsAdapter(itemList){id, item ->
            GoodItemCreationDialog.create(
                context = this,
                inflater = layoutInflater,
                item = item,
                onSaveButtonPressed = { item ->
                }, onUpdateItem = { item ->
                    // update item
                    logPrint(item.toString())
                    viewModel.updateNomeclatureItem(
                        id,
                        item,
                        onSuccess = {
                            viewModel.selectAllNomenclature()
                        },
                        onFailure = {
                            toast(it)
                        })

                }
            )
        }
        binding.goodsItemRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.goodsItemRecyclerView.adapter = adapter
//        val dividerItemDecoration =
//            DividerItemDecoration(this, RecyclerView.VERTICAL)
//        binding.goodsItemRecyclerView.addItemDecoration(dividerItemDecoration)
    }
}
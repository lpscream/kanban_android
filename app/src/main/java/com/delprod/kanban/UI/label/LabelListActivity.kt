package com.delprod.kanban.UI.label

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.delprod.kanban.R
import com.delprod.kanban.UI.label.dialogs.PrinterListDialog
import com.delprod.kanban.UI.label.dialogs.SimpleLabelEditorDialog
import com.delprod.kanban.core.App
import com.delprod.kanban.core.logPrint
import com.delprod.kanban.databinding.ActivityLabelListBinding
import com.delprod.kanban.utils.launchAndCollectIn
import com.delprod.kanban.utils.toast

class LabelListActivity : AppCompatActivity() {
    private lateinit var viewModel: LabelViewModel
    private val binding by lazy { ActivityLabelListBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewModel = (this.application as App).labelViewModel
        viewModel.fetchLabelListNames()
        viewModel.uiLabelNamesState.launchAndCollectIn(this){
            when(it){
                is LabelListUiState.Idle -> {
                    binding.progressBar.isVisible = false
                    binding.errorMessage.isVisible = false
                    binding.labelList.isVisible = false
                }
                is LabelListUiState.Error -> {
                    logPrint(it.message)
                    binding.labelList.isVisible = false
                    binding.errorMessage.isVisible = true
                    binding.progressBar.isVisible = false
                    binding.errorMessage.setText(it.message)
                }
                is LabelListUiState.Loaded -> {
                    if (!it.items.isEmpty()){
                        binding.errorMessage.isVisible = false
                        binding.progressBar.isVisible = false
                        binding.labelList.isVisible = true
                        val adapter = LabelNameListAdapter(
                            it.items,
                            onClickItem = {
                                val intent =
                                    Intent(this@LabelListActivity, PrintLabelActivity::class.java)
                                intent.putExtra("LABEL_UUID", it.uuid)
                                startActivity(intent)
                            },
                            onEditLabel = {
                                val intent =
                                    Intent(this@LabelListActivity, LabelEditorActivity::class.java)
                                intent.putExtra("LABEL_UUID", it.uuid)
                                startActivity(intent)
                            },
                            onAddOnBasicLabel = {
                                viewModel.addOnBasicLabel(
                                    it,
                                    onSuccess = { viewModel.fetchLabelListNames() },
                                    onFailure = { toast(it) }
                                )
                            },
                            onFastEditLabel = {
                                viewModel.fetchLabelForEdit(
                                    it.uuid,
                                    onSuccess = {
                                        SimpleLabelEditorDialog.create(
                                            this@LabelListActivity,
                                            it,
                                            onInsert = {  },
                                            onUpdate = {viewModel.updateLabel(
                                                it,
                                                onSuccess = { viewModel.fetchLabelListNames() },
                                                onFailure = { toast(it)}
                                            )},
                                        )
                                    },
                                    onFailure = {toast(it)}
                                )
                            }
                        )
                        binding.labelList.layoutManager = LinearLayoutManager(this@LabelListActivity)
                        binding.labelList.adapter = adapter
                    }else{
                        binding.labelList.isVisible = false
                        binding.errorMessage.isVisible = true
                        binding.progressBar.visibility = View.GONE
                        binding.errorMessage.setText(application.resources.getString(R.string.empty_label_name_list_message))
                    }
                }
                is LabelListUiState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.errorMessage.isVisible = false
                    binding.labelList.isVisible = false
                }
            }
        }

        binding.addLabelBtn.setOnClickListener {
            SimpleLabelEditorDialog.create(
                this,
                null,
                onInsert = {
                    viewModel.insertLabel(
                        it,
                        onSuccess = { viewModel.fetchLabelListNames() },
                        onFailure = {toast(it)})
                },
                onUpdate = {})

        //            val intent = Intent(this@LabelListActivity, LabelEditorActivity::class.java)
//            startActivity(intent)
        }


        binding.printerBtn.setOnClickListener {
            PrinterListDialog.create(
                this@LabelListActivity,
                layoutInflater,
                viewModel
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchLabelListNames()
    }
}
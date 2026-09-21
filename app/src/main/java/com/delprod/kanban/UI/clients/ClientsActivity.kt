package com.delprod.kanban.UI.clients

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.delprod.kanban.R
import com.delprod.kanban.UI.clients.adapter.ClientsAdapter
import com.delprod.kanban.core.App
import com.delprod.kanban.databinding.ActivityClientsBinding
import com.delprod.kanban.db.SubdivisionEntity
import com.delprod.kanban.utils.launchAndCollectIn
import com.delprod.kanban.utils.toast

class ClientsActivity : AppCompatActivity() {

    private val binding by lazy { ActivityClientsBinding.inflate(layoutInflater) }
    private lateinit var adapter: ClientsAdapter
    private lateinit var viewModel: ClientsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = (this.application as App).clientsViewModel
        viewModel.fetchClientList()
        bindViewModel()

        binding.addClientBtn.setOnClickListener {
            //show add client dialog
            ClientDialog.create(
                this@ClientsActivity,
                layoutInflater,
                null,
                onSaveBtnPressed = {
                    viewModel.insertClient(
                        it,
                        onSuccess = {viewModel.fetchClientList()},
                        onFailure = {toast(it)})
                },
                onDeletePressed = {}
            )
        }
    }

    private fun bindViewModel() {
        viewModel.uiClintListState.launchAndCollectIn(this){
            when(it){
                is ClientListUiState.Idle -> {
                    binding.message.isVisible = false
                    binding.clientList.isVisible = false
                    binding.progressBar.isVisible = false
                }
                is ClientListUiState.Loading -> {
                    binding.message.isVisible = false
                    binding.clientList.isVisible = false
                    binding.progressBar.isVisible = true
                }
                is ClientListUiState.Loaded -> {
                    binding.message.isVisible = false
                    binding.clientList.isVisible = true
                    binding.progressBar.isVisible = false
                    initAdapter(it.clients)
                }
                is ClientListUiState.Error -> {
                    binding.message.isVisible = true
                    binding.clientList.isVisible = false
                    binding.progressBar.isVisible = false
                    binding.message.setText(it.message)
                }
            }
        }
    }

    private fun initAdapter(items: List<SubdivisionEntity>) {
        adapter = ClientsAdapter(items){
            ClientDialog.create(
                this@ClientsActivity,
                layoutInflater,
                it,
                onSaveBtnPressed = {
                    toast(it.toString())
                },
                onDeletePressed = {
                    viewModel.deleteClient(
                        it,
                        onSuccess = {viewModel.fetchClientList()},
                        onFailure = {toast(it)})
                }
            )
        }
        binding.clientList.layoutManager = LinearLayoutManager(this)
        binding.clientList.adapter = adapter
    }
}
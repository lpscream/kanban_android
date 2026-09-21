package com.delprod.kanban.UI.clients.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.delprod.kanban.databinding.ClientListItemBinding
import com.delprod.kanban.db.SubdivisionEntity

class ClientsAdapter(
    private val items: List<SubdivisionEntity>,
    private val onClick: (SubdivisionEntity) -> Unit):
RecyclerView.Adapter<ClientsAdapter.ClientItemViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ClientItemViewHolder {
        val binding = ClientListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ClientItemViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ClientItemViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size


    inner class ClientItemViewHolder(private val binding: ClientListItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: SubdivisionEntity){
            binding.clientNumber.setText(item.id.toString())
            binding.clientCode.setText(item.code)
            binding.clientFullLabel.setText(item.fullLabel)
            binding.clientRegion.setText(item.region)
            binding.clientResponsiblePerson.setText(item.responsiblePerson)
            binding.clientPhone.setText(item.phone)
            binding.clientUnitId.setText(item.unitId)
            binding.clientCategory.setText(item.category)
            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }
}
package com.delprod.kanban.UI.goods

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.delprod.kanban.core.logPrint
import com.delprod.kanban.data.goods.ProductModel
import com.delprod.kanban.databinding.NomeclatureListItemBinding
import com.delprod.kanban.db.NomenclatureEntity

class GoodsAdapter(
    private val itemsList: List<NomenclatureEntity>,
    private val onClickItem: (Long, NomenclatureEntity) -> Unit
): RecyclerView.Adapter<GoodsAdapter.GoodsViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GoodsViewHolder {
        val binding = NomeclatureListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GoodsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoodsViewHolder, position: Int){
        holder.bind(itemsList[position])
    }

    override fun getItemCount(): Int = itemsList.size


    inner class GoodsViewHolder(private val binding: NomeclatureListItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: NomenclatureEntity){
            binding.article.setText(item.code.toString())
            binding.name.setText(item.name)
            binding.pluCode.setText(item.plu.toString())
            binding.root.setOnClickListener {
                onClickItem(item.id,NomenclatureEntity(
                    id = item.id,
                    name = item.name,
                    code = item.code,
                    unit = item.unit,
                    plu = item.plu
                ))
            }
        }
    }

}






package com.example.renerd.features.episodes.components.filters_dialog.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.renerd.R
import com.example.renerd.core.extentions.ContextManager
import com.example.renerd.view_models.FiltersTabsItemModel
import core.extensions.styleBackground
import core.extensions.toTitleCase

class FilterItemAdapter(
    private val filtersTabsListItemModel: List<FiltersTabsItemModel>,
    private val onClick: (FiltersTabsItemModel) -> Unit
) : RecyclerView.Adapter<FilterItemAdapter.FilterItemViewHolder>() {

    // Não é mais necessário, pois o status agora está no FiltersTabsItemModel
    // var currentStatus: Boolean = true

    class FilterItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //val main_container: LinearLayout = itemView.findViewById(R.id.main_container)
        val textView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.r_filter_item_adapter, parent, false)
        return FilterItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FilterItemViewHolder, position: Int) {
        val item = filtersTabsListItemModel[position]
        holder.textView.text = item.label.toTitleCase()

        styleOnStatus(item.status, holder)

        // Adiciona um OnClickListener para alternar o status
        holder.textView.setOnClickListener {
            item.status = !item.status
            onClick(item)
            //notifyItemChanged(position)
            styleOnStatus(item.status, holder)
        }
    }

    private fun styleOnStatus(status: Boolean, holder: FilterItemViewHolder){
        if (status) {
            holder.textView.styleBackground(
                backgroundColor = ContextManager.getColorHex(6),
                radius = 100f
            )
            holder.textView.setTextColor(Color.parseColor(ContextManager.getColorHex(1)))
        } else {
            holder.textView.styleBackground(
                backgroundColor = ContextManager.getColorHex(2),
                radius = 100f
            )
            holder.textView.setTextColor(Color.parseColor(ContextManager.getColorHex(5)))
        }
    }

    override fun getItemCount(): Int {
        return filtersTabsListItemModel.size
    }
}
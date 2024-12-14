package com.example.renerd.components.filters_dialog.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.renerd.R
import com.example.renerd.core.utils.log
import com.example.renerd.view_models.FiltersTabsItemModel
import core.extensions.toTitleCase

class FilterItemAdapter(
    private val filtersTabsListItemModel: List<FiltersTabsItemModel>,
    private val onClick: (FiltersTabsItemModel) -> Unit
) : RecyclerView.Adapter<FilterItemAdapter.FilterItemViewHolder>() {

    class FilterItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val textView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.r_filter_item_adapter, parent, false)
        return FilterItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FilterItemViewHolder, position: Int) {
        val item = filtersTabsListItemModel[position]
        holder.textView.text = item.label.toTitleCase()

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = item.status

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.status = isChecked
            onClick(item)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return filtersTabsListItemModel.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll(select: Boolean) {
        for (item in filtersTabsListItemModel) {
            item.status = select
        }
        notifyDataSetChanged()
    }
}
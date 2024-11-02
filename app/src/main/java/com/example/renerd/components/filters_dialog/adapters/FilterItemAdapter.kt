package com.example.renerd.components.filters_dialog.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.renerd.R
import com.example.renerd.view_models.FiltersTabsListItemModel

class FilterItemAdapter(
    private val filtersTabsListItemModelList: List<FiltersTabsListItemModel>,
    private val onClick: (FiltersTabsListItemModel) -> Unit
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
        val currentItem = filtersTabsListItemModelList[position]
        holder.textView.text = currentItem.label
        holder.checkBox.isChecked = currentItem.status

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            currentItem.status = isChecked
            onClick(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return filtersTabsListItemModelList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll(select: Boolean) {
        for (item in filtersTabsListItemModelList) {
            item.status = select
        }
        notifyDataSetChanged()
    }
}
package com.example.renerd.components.filters_dialog

import com.example.renerd.view_models.FiltersTabsItemModel

interface FilterTabListener {
    fun onItemValeuChange(filtersTabsItemModel: FiltersTabsItemModel)
}
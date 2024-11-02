package com.example.renerd.components.filters_dialog

import com.example.renerd.view_models.FiltersTabsListItemModel

interface FilterTabListener {
    fun onItemValeuChange(filtersTabsListItemModel: FiltersTabsListItemModel)
}
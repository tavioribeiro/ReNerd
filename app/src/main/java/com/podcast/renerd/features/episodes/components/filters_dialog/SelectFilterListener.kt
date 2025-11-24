package com.podcast.renerd.features.episodes.components.filters_dialog

import com.podcast.renerd.view_models.FiltersTabsItemModel

interface FilterTabListener {
    fun onItemValeuChange(filtersTabsItemModel: FiltersTabsItemModel)
}
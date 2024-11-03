package com.example.renerd.features.episodes

import com.example.renerd.view_models.EpisodeViewModel
import com.example.renerd.view_models.FiltersTabsItemModel
import com.example.renerd.view_models.FiltersTabsListModel


interface EpisodesContract {

    interface View {
        fun setUpFilterModal(filtersTabsListModel:FiltersTabsListModel)
        fun showEpisodes(episodes: MutableList<EpisodeViewModel>)
        fun showError(message: String)
        fun showLoading()
        fun hideLoading()
        fun showActionButtons(tempFiltersTabsListModel:FiltersTabsListModel)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()

        fun loadEpisodes()
        fun loadLastEpisodes()


        fun getFiltersTabsList()
        fun updateFiltersTabsList(tempFiltersTabsListModel:FiltersTabsListModel)
        fun updateFiltersTabsItemList(mixedFiltersTabsItemModel: MutableList<FiltersTabsItemModel>)
    }

    interface Repository {
        suspend fun getEpisodes(): MutableList<EpisodeViewModel>

        suspend fun insertFilterTabItem(filtersTabsItemModel: FiltersTabsItemModel)
        suspend fun getAllFilterTabItems(): MutableList<FiltersTabsItemModel>
        suspend fun updateFilterTabItem(filtersTabsItemModel: FiltersTabsItemModel)
/*
        suspend fun insertFilterProductTabItem(filtersTabsListItemModel: FiltersTabsListItemModel)
        suspend fun getAllFilterProductTabItems(): MutableList<FiltersTabsListItemModel>

        suspend fun insertFilterSubjectTabItem(filtersTabsListItemModel: FiltersTabsListItemModel)
        suspend fun getAllFilterSubjectTabItems(): MutableList<FiltersTabsListItemModel>

        suspend fun insertFilterGuestTabItem(filtersTabsListItemModel: FiltersTabsListItemModel)
        suspend fun getAllFilterGuestTabItems(): MutableList<FiltersTabsListItemModel>

        suspend fun insertFilterYearTabItem(filtersTabsListItemModel: FiltersTabsListItemModel)
        suspend fun getAllFilterYearTabItems(): MutableList<FiltersTabsListItemModel>
*/
        suspend fun getLastEpisodes(): MutableList<EpisodeViewModel>
    }
}
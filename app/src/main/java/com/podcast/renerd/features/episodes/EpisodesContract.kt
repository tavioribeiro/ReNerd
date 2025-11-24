package com.podcast.renerd.features.episodes

import com.podcast.renerd.view_models.EpisodeViewModel
import com.podcast.renerd.view_models.FiltersTabsItemModel
import com.podcast.renerd.view_models.FiltersTabsListModel

interface EpisodesContract {

    interface View {
        fun setListsData(
            tempFiltersTabsListModel: FiltersTabsListModel? = null,
            tempEpisodesList: List<EpisodeViewModel>? = null
        )
        fun setUpFilterModal(filtersTabsListModel: FiltersTabsListModel)
        fun setUpSearchModal(episodesList: List<EpisodeViewModel>)
        fun showEpisodes(episodes: MutableList<EpisodeViewModel>, scrollTo: Int = 0)
        fun showError(message: String)
        fun showLoading()
        fun hideLoading()
        fun setUpFilterButton()
        fun setUpSearchButton()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadData()
        fun updateFiltersTabsItemList(mixedFiltersTabsItemModel: MutableList<FiltersTabsItemModel>)
        fun recyclerviewEpisodesCurrentPosition(currentPosition: Int)
        fun updateFiltersTabsList(tempFiltersTabsListModel: FiltersTabsListModel)
        fun loadLastEpisodes()
    }

    interface Repository {
        suspend fun getEpisodes(): MutableList<EpisodeViewModel>
        suspend fun insertFilterTabItem(filtersTabsItemModel: FiltersTabsItemModel)
        suspend fun getAllFilterTabItems(): MutableList<FiltersTabsItemModel>
        suspend fun updateFilterTabItem(filtersTabsItemModel: FiltersTabsItemModel)
        suspend fun getLastEpisodes(): MutableList<EpisodeViewModel>
        fun setRecyclerviewEpisodesCurrentPosition(currentPosition: Int)
        fun getRecyclerviewEpisodesCurrentPosition(): String
    }
}

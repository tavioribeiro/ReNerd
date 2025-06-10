package com.example.renerd.features.episodes.components.search_dialog

import com.example.renerd.view_models.EpisodeViewModel
import com.example.renerd.view_models.FiltersTabsItemModel
import com.example.renerd.view_models.FiltersTabsListModel

interface SearchDialogContract {

    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun showEpisodes(episodes: List<EpisodeViewModel>, currentPosition: Int)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun searchEpisodesByName(query: String)
    }
}

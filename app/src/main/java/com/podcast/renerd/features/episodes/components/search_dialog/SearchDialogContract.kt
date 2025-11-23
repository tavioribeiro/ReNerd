package com.podcast.renerd.features.episodes.components.search_dialog

import com.podcast.renerd.view_models.EpisodeViewModel
import com.podcast.renerd.view_models.FiltersTabsItemModel
import com.podcast.renerd.view_models.FiltersTabsListModel

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

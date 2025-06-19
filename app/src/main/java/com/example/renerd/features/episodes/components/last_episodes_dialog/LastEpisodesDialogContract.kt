package com.example.renerd.features.episodes.components.last_episodes_dialog

import com.example.renerd.view_models.EpisodeViewModel
import com.example.renerd.view_models.FiltersTabsItemModel
import com.example.renerd.view_models.FiltersTabsListModel

interface LastEpisodesDialogContract {

    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun showEpisodes(episodes: List<EpisodeViewModel>, currentPosition: Int)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun lastEpisodesEpisodesByName(query: String)
    }
}

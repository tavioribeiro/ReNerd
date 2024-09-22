package com.example.renerd.features.episodes

import com.example.renerd.view_models.EpisodeViewModel


interface EpisodesContract {

    interface View {
        fun showEpisodes(episodes: MutableList<EpisodeViewModel>)
        fun showError(message: String)
        fun showLoading()
        fun hideLoading()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadEpisodes()
    }

    interface Repository {
        suspend fun getEpisodes(): MutableList<EpisodeViewModel>
    }
}
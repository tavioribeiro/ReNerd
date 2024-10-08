package com.example.renerd.features.episodes

import android.content.Context
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
        fun loadLastEpisodes()
    }

    interface Repository {
        suspend fun getEpisodes(): MutableList<EpisodeViewModel>
        suspend fun getLastEpisodes(): MutableList<EpisodeViewModel>
    }
}
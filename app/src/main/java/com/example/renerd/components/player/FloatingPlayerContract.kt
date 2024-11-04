package com.example.renerd.components.player

import com.example.renerd.view_models.EpisodeViewModel


interface FloatingPlayerContract {
    interface View {
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()

        fun getEpisodeById(id: Long): EpisodeViewModel
        fun updateEpisode(episode: EpisodeViewModel)
    }


    interface Repository {
        suspend fun setCurrentEpisodePlaying(url: String)
        fun getCurrentEpisodePlaying(): String
        fun setCurrentEpisodePosition(url: String)
        fun getCurrentEpisodePosition(): String

        fun getEpisodeById(id: Long): EpisodeViewModel
        fun updateEpisode(episode: EpisodeViewModel)
    }
}

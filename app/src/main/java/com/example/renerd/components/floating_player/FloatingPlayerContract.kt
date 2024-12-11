package com.example.renerd.components.floating_player

import com.example.renerd.view_models.EpisodeViewModel


interface FloatingPlayerContract {
    interface View {
        fun updateInfosUi(episode: EpisodeViewModel)
        fun showUi()
        fun updateCurrentEpisode(episode: EpisodeViewModel)
        fun updateButtonsUi(isPlaying: Boolean, currentTime: Int, totalTime: Int)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()

        fun setCurrentPlayingEpisodeId(episode: EpisodeViewModel)
        fun getCurrentPlayingEpisode()
        fun getEpisodeById(id: Long): EpisodeViewModel
        fun updateEpisode(episode: EpisodeViewModel)
    }


    interface Repository {
        fun setCurrentEpisodePlayingId(id: Int)
        fun getCurrentEpisodePlayingId(): Int

        fun getEpisodeById(id: Long): EpisodeViewModel
        fun updateEpisode(episode: EpisodeViewModel)
    }
}

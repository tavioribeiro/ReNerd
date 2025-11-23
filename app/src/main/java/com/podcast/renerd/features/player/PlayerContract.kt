package com.podcast.renerd.features.player


interface PlayerContract {
    interface View {

    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun setCurrentEpisodePlaying(url: String)
        fun getCurrentEpisodePlaying(): String
        fun setCurrentEpisodePosition(url: String)
        fun getCurrentEpisodePosition(): String
    }


    interface Repository {
        suspend fun setCurrentEpisodePlaying(url: String)
        fun getCurrentEpisodePlaying(): String
        fun setCurrentEpisodePosition(url: String)
        fun getCurrentEpisodePosition(): String
    }
}

package com.example.renerd.features.player

import android.content.Context


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
        fun setContext(context: Context)
        suspend fun setCurrentEpisodePlaying(url: String)
        fun getCurrentEpisodePlaying(): String
        fun setCurrentEpisodePosition(url: String)
        fun getCurrentEpisodePosition(): String
    }
}

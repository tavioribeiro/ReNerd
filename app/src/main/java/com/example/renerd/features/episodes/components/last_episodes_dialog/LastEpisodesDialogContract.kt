package com.example.renerd.features.episodes.components.last_episodes_dialog

import com.example.renerd.view_models.EpisodeViewModel

interface LastEpisodesDialogContract {

    interface View {
        fun showLoading(isLoading: Boolean)
        fun displayNewEpisodes(episodes: List<EpisodeViewModel>)
        fun showFeedbackMessage(message: String)
        fun setSaveButtonEnabled(isEnabled: Boolean)
        fun closeView()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadNewEpisodes()
        fun onSaveButtonClicked(episodesToSave: List<EpisodeViewModel>)
    }

    interface Repository {
        suspend fun fetchLastEpisodesSinceLastUpdate(): List<EpisodeViewModel>
        suspend fun saveNewEpisodes(episodes: List<EpisodeViewModel>)
    }
}
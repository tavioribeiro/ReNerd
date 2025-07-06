package com.example.renerd.features.episodes.components.last_episodes_dialog

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.renerd.core.utils.log
import com.example.renerd.view_models.EpisodeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LastEpisodesDialogPresenter(
    private val repository: LastEpisodesDialogContract.Repository
) : LastEpisodesDialogContract.Presenter {

    private var view: LastEpisodesDialogContract.View? = null
    private val presenterScope = CoroutineScope(Job() + Dispatchers.Main)

    override fun attachView(view: LastEpisodesDialogContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun loadNewEpisodes() {
        presenterScope.launch {
            view?.showLoading(true)
            try {
                val newEpisodes = repository.fetchLastEpisodesSinceLastUpdate()
                view?.showLoading(false)

                if (newEpisodes.isNotEmpty()) {
                    view?.displayNewEpisodes(newEpisodes)
                    view?.setSaveButtonEnabled(true)
                } else {
                    view?.displayNewEpisodes(emptyList())
                    view?.setSaveButtonEnabled(false)
                    view?.showFeedbackMessage("Nenhum episódio novo encontrado.")
                    view?.closeView()
                }
            } catch (e: Exception) {
                view?.showLoading(false)
                view?.setSaveButtonEnabled(false)
                view?.showFeedbackMessage("Erro ao buscar episódios.")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSaveButtonClicked(episodesToSave: List<EpisodeViewModel>) {
        presenterScope.launch {
            view?.showLoading(true)
            try {
                repository.saveNewEpisodes(episodesToSave)
                withContext(Dispatchers.Main) {
                    view?.showLoading(false)
                    view?.showFeedbackMessage("Episódios salvos com sucesso!")
                    view?.closeView()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view?.showLoading(false)
                    view?.showFeedbackMessage("Falha ao salvar os episódios.")
                }
            }
        }
    }
}
package com.example.renerd.features.episodes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class EpisodesPresenter(private val repository: EpisodesContract.Repository) : EpisodesContract.Presenter {

    private var view: EpisodesContract.View? = null

    override fun attachView(view: EpisodesContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

    override fun loadEpisodes() {
        view?.showLoading()
        try {
            CoroutineScope(Dispatchers.Main).launch {
                val episodes = repository.getEpisodes()
                view?.showEpisodes(episodes)
                view?.hideLoading()
            }
        } catch (e: Exception) {
            view?.showError("Erro ao carregar episódios")
        }
    }
}

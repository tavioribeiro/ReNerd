package com.example.renerd.features.episodes



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
            val episodes = repository.getEpisodes()
            view?.showEpisodes(episodes)
        } catch (e: Exception) {
            view?.showError("Erro ao carregar episódios")
        } finally {
            view?.hideLoading()
        }
    }
}

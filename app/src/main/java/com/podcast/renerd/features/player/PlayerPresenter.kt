package com.podcast.renerd.features.player


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PlayerPresenter(
    private val repository: PlayerContract.Repository
) : PlayerContract.Presenter {
    private var view: PlayerContract.View? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun attachView(view: PlayerContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
        scope.cancel()
    }

    override fun setCurrentEpisodePlaying(url: String) {
        scope.launch {
            repository.setCurrentEpisodePlaying(url)
        }
    }

    override fun getCurrentEpisodePlaying(): String {
        return repository.getCurrentEpisodePlaying()
    }

    override fun setCurrentEpisodePosition(url: String) {
        TODO("Not yet implemented")
    }

    override fun getCurrentEpisodePosition(): String {
        TODO("Not yet implemented")
    }
}

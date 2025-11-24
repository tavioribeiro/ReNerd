package com.podcast.renerd.features.player


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class PlayerPresenter(private val repository: PlayerContract.Repository): PlayerContract.Presenter {
    private var view: PlayerContract.View? = null

    override fun attachView(view: PlayerContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

    override fun setCurrentEpisodePlaying(url: String){
        CoroutineScope(Dispatchers.Main).launch {
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

package com.example.renerd.components.player


import com.example.renerd.view_models.EpisodeViewModel


class FloatingPlayerPresenter(private val repository: FloatingPlayerContract.Repository): FloatingPlayerContract.Presenter {
    private var view: FloatingPlayerContract.View? = null

    override fun attachView(view: FloatingPlayerContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }


/*
    override fun getCurrentEpisodePlaying(episode: EpisodeViewModel)//: EpisodeViewModel
    {
    }

    override fun setCurrentEpisodePlaying(episode: EpisodeViewModel){
    }

 */



    override fun getEpisodeById(id: Long): EpisodeViewModel{
        return repository.getEpisodeById(id)
    }

    override fun updateEpisode(episode: EpisodeViewModel){
        repository.updateEpisode(episode)
    }
}

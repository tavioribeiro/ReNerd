package com.podcast.renerd.features.episodes.components.floating_player


import com.podcast.renerd.view_models.EpisodeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FloatingPlayerPresenter(private val repository: FloatingPlayerContract.Repository):
    FloatingPlayerContract.Presenter {
    private var view: FloatingPlayerContract.View? = null

    override fun attachView(view: FloatingPlayerContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

    override fun setCurrentPlayingEpisodeId(episode: EpisodeViewModel){
        try {
            CoroutineScope(Dispatchers.Main).launch {
                repository.setCurrentEpisodePlayingId(episode.id)
            }
        } catch (e: Exception) {
            //view?.showError("Erro ao carregar episódios")
        }
    }






    override fun getCurrentPlayingEpisode() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentEpisodePlayingId = repository.getCurrentEpisodePlayingId()
                val currentEpisodePlaying = repository.getEpisodeById(currentEpisodePlayingId.toLong())

                if (currentEpisodePlaying.id != 0) {
                    withContext(Dispatchers.Main) {
                        view?.updateCurrentEpisode(currentEpisodePlaying)
                        view?.updateInfosUi(currentEpisodePlaying)
                        view?.updateButtonsUi(false, currentEpisodePlaying.elapsedTime, currentEpisodePlaying.duration)
                        view?.showUi()
                        view?.updatePlayerTimerUI(currentEpisodePlaying.elapsedTime, currentEpisodePlaying.duration)
                    }
                }
            } catch (e: Exception) {
                // silently ignore if no episode was previously playing
            }
        }
    }

    override fun getEpisodeById(id: Long): EpisodeViewModel {
        return repository.getEpisodeById(id)
    }

    override fun updateEpisode(episode: EpisodeViewModel) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.updateEpisode(episode)
            } catch (e: Exception) {
                // ignore progress save errors
            }
        }
    }
}

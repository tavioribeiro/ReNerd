package com.example.renerd.features.episodes.components.search_dialog

import com.example.renerd.core.utils.log
import com.example.renerd.features.episodes.EpisodesContract
import com.example.renerd.view_models.EpisodeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.Normalizer

class SearchDialogPresenter(private val repository: EpisodesContract.Repository) : SearchDialogContract.Presenter {

    private var view: SearchDialogContract.View? = null
    private var allEpisodes: List<EpisodeViewModel> = emptyList() // Cache para todos os episódios

    override fun attachView(view: SearchDialogContract.View) {
        this.view = view
        loadAllEpisodes() // Carrega todos os episódios ao attachar a view para a busca
    }

    override fun detachView() {
        this.view = null
    }

    private fun loadAllEpisodes() {
        view?.showLoading()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                allEpisodes = repository.getEpisodes() // Carrega todos os episódios
                view?.hideLoading() // Esconde loading após carregar inicialmente
            } catch (e: Exception) {
                view?.showError("Erro ao carregar episódios para busca.")
                log(e)
                view?.hideLoading()
            }
        }
    }


    override fun searchEpisodesByName(query: String) {
        if (query.isEmpty()) {
            view?.showEpisodes(emptyList(), 0) // Mostra lista vazia se a query estiver vazia
            return
        }

        view?.showLoading()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val filteredEpisodes = filterEpisodesByName(allEpisodes, query)
                view?.showEpisodes(filteredEpisodes, 0) // Posição 0 para nova busca
            } catch (e: Exception) {
                view?.showError("Erro ao buscar episódios.")
                log(e)
            } finally {
                view?.hideLoading()
            }
        }
    }

    private fun filterEpisodesByName(episodes: List<EpisodeViewModel>, query: String): List<EpisodeViewModel> {
        val normalizedQuery = normalizeString(query)

        return episodes.filter { episode ->
            val normalizedTitle = normalizeString(episode.title)
            normalizedTitle.contains(normalizedQuery)
        }
    }

    // Função utilitária para normalizar strings (remover acentos e case insensitive)
    private fun normalizeString(str: String): String {
        val normalized = Normalizer.normalize(str, Normalizer.Form.NFD)
        return "[^\\p{ASCII}]".toRegex().replace(normalized, "").lowercase()
    }
}
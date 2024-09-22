package com.example.renerd.features.episodes

import com.example.renerd.view_models.EpisodeViewModel

class EpisodesRepository : EpisodesContract.Repository {

    override fun getEpisodes(): List<EpisodeViewModel> {
        // Implemente a lógica para buscar os episódios
        // de uma fonte de dados (API, banco de dados, etc.)
        return listOf(
            // Exemplo de dados
            EpisodeViewModel(1, "Título 1", "Descrição 1", "", "", 0, "", ""),
            EpisodeViewModel(2, "Título 2", "Descrição 2", "", "", 0, "", "")
        )
    }
}


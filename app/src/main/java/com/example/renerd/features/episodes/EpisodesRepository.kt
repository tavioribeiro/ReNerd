package com.example.renerd.features.episodes

import com.example.renerd.core.network.PodcastClient
import com.example.renerd.core.utils.log
import com.example.renerd.view_models.EpisodeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.URLDecoder
import kotlin.random.Random

class EpisodesRepository: EpisodesContract.Repository {

    override suspend fun getEpisodes(): MutableList<EpisodeViewModel> {
        return withContext(Dispatchers.IO) { // Usando withContext para garantir que a rede seja acessada em uma thread apropriada
            try {
                val after = "2000-01-01%2000%3A00%3A00"
                val before = "2024-09-13%2000%3A00%3A00"

                val afterDecoded = URLDecoder.decode(after, "UTF-8")
                val beforeDecoded = URLDecoder.decode(before, "UTF-8")
                val response = PodcastClient.api.getNerdcasts(after = afterDecoded, before = beforeDecoded).execute() // Executa a chamada
                if (response.isSuccessful) {
                    val podcasts = response.body()
                    val episodesViewModel = mutableListOf<EpisodeViewModel>()
                    if (!podcasts.isNullOrEmpty()) {
                        for (episode in podcasts) {
                            val episodeViewModel = EpisodeViewModel(
                                id = episode.id ?: 0,
                                title = episode.title ?: "",
                                description = episode.description ?: "",
                                imageUrl = episode.image ?: "",
                                audioUrl = episode.audioHigh ?: "",
                                duration = episode.duration ?: 0,
                                publishedAt = episode.publishedAt ?: "",
                                category = episode.category ?: ""
                            )

                            episodesViewModel.add(episodeViewModel)
                        }
                    }
                    episodesViewModel // Retorna a lista aqui
                } else {
                    mutableListOf() // Retorna uma lista vazia em caso de erro
                }
            } catch (e: SocketTimeoutException) {
                log(e)
                mutableListOf() // Retorna uma lista vazia em caso de erro
            } catch (e: Exception) {
                log(e)
                mutableListOf() // Retorna uma lista vazia em caso de erro
            }
        }
    }
}

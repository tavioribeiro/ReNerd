package com.example.renerd.features.episodes

import com.example.renerd.core.utils.log
import com.example.renerd.view_models.EpisodeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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


                val productsList = extractUniqueProducts(episodes)
                val subjectsList = extractUniqueSubjects(episodes)
                val guestsList = extractUniqueGuests(episodes)
                val yearsList = extractUniqueYears(episodes)



                val filteredByProducts = filterEpisodesByProductsInclude(episodes, listOf("nerdcast", "nerdtech")).toMutableList() ?: mutableListOf<EpisodeViewModel>()
                val filteredBySubjects = filterEpisodesBySubjectInclude(filteredByProducts, listOf("Ciências, Cinema")).toMutableList() ?: mutableListOf<EpisodeViewModel>()
                val filteredByGuests = filterEpisodesByGuestInclude(filteredBySubjects, listOf("Affonso Solano")).toMutableList() ?: mutableListOf<EpisodeViewModel>()
                val filteredByYears = filterEpisodesByYearInclude(filteredByGuests, listOf("2019", "2012")).toMutableList() ?: mutableListOf<EpisodeViewModel>()
                log(filteredByYears)

                view?.showEpisodes(filteredByGuests)
                view?.hideLoading()
            }
        } catch (e: Exception) {
            view?.showError("Erro ao carregar episódios")
        }
    }


    override fun loadLastEpisodes() {
        try {
            CoroutineScope(Dispatchers.Main).launch {
                val episodes = repository.getLastEpisodes()

                val filteredByProducts = filterEpisodesByProductsInclude(episodes, listOf("nerdcast", "nerdtech")).toMutableList() ?: mutableListOf<EpisodeViewModel>()
                val filteredBySubjects = filterEpisodesBySubjectInclude(filteredByProducts, listOf("Ciências, Cinema")).toMutableList() ?: mutableListOf<EpisodeViewModel>()
                val filteredByGuests = filterEpisodesByGuestInclude(filteredBySubjects, listOf("Affonso Solano")).toMutableList() ?: mutableListOf<EpisodeViewModel>()

                view?.showEpisodes(filteredByGuests)
            }
        } catch (e: Exception) {
            view?.showError("Erro ao carregar episódios")
        }
    }



    private fun extractUniqueProducts(episodes: List<EpisodeViewModel>): List<String> {
        return episodes.map { it.product }.distinct()
    }

    private fun filterEpisodesByProductsInclude(episodes: List<EpisodeViewModel>, products: List<String>): List<EpisodeViewModel> {
        return episodes.filter { episode -> products.contains(episode.product) }
    }

    private fun filterEpisodesByProductsExclude(episodes: List<EpisodeViewModel>, products: List<String>): List<EpisodeViewModel> {
        return episodes.filter { episode -> !products.contains(episode.product) }
    }





    fun extractUniqueSubjects(episodes: List<EpisodeViewModel>): List<String> {
        return episodes
            .flatMap { it.subject.split(",") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    fun filterEpisodesBySubjectInclude(episodes: List<EpisodeViewModel>, subjectsToInclude: List<String>): List<EpisodeViewModel> {
        val normalizedSubjectsToInclude = subjectsToInclude
            .flatMap { it.split(",") }
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

        return episodes.filter { episode ->
            val episodeSubjects = episode.subject.split(",").map { it.trim().lowercase() }
            episodeSubjects.any { it in normalizedSubjectsToInclude }
        }
    }

    private fun filterEpisodesBySubjectExclude(episodes: List<EpisodeViewModel>, subjectsToExclude: List<String>): List<EpisodeViewModel> {
        val normalizedSubjectsToExclude = subjectsToExclude
            .flatMap { it.split(",") }
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

        return episodes.filterNot { episode ->
            val episodeSubjects = episode.subject.split(",").map { it.trim().lowercase() }
            episodeSubjects.any { it in normalizedSubjectsToExclude }
        }
    }







    fun extractUniqueGuests(episodes: List<EpisodeViewModel>): List<String> {
        //episodes.forEachIndexed { index, episodeViewModel -> log(episodes[index].guests) }
        return episodes
            .flatMap { it.guests.split(",") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    fun filterEpisodesByGuestInclude(episodes: List<EpisodeViewModel>, guests: List<String>): List<EpisodeViewModel> {
        val normalizedGuests = guests
            .flatMap { it.split(",") }
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

        return episodes.filter { episode ->
            val episodeGuests = episode.guests.split(",").map { it.trim().lowercase() }
            episodeGuests.any { it in normalizedGuests }
        }
    }

    private fun filterEpisodesByGuestExclude(episodes: List<EpisodeViewModel>, guests: List<String>): List<EpisodeViewModel> {
        val normalizedGuests = guests
            .flatMap { it.split(",") }
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

        return episodes.filterNot { episode ->
            val episodeGuests = episode.guests.split(",").map { it.trim().lowercase() }
            episodeGuests.any { it in normalizedGuests }
        }
    }







    private fun extractUniqueYears(episodes: List<EpisodeViewModel>): List<String> {
        return episodes.map { it.publishedAt.substring(0, 4) }.distinct().sorted()
    }

    private fun filterEpisodesByYearInclude(episodes: List<EpisodeViewModel>, years: List<String>): List<EpisodeViewModel> {
        return episodes.filter { episode -> years.contains(episode.publishedAt.substring(0, 4)) }
    }

    private fun filterEpisodesByYearExclude(episodes: List<EpisodeViewModel>, years: List<String>): List<EpisodeViewModel> {
        return episodes.filterNot { episode -> years.contains(episode.publishedAt.substring(0, 4)) }
    }
}

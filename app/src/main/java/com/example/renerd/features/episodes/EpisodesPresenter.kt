package com.example.renerd.features.episodes

import com.example.renerd.core.utils.log
import com.example.renerd.view_models.EpisodeViewModel
import com.example.renerd.view_models.FiltersTabsListItemModel
import com.example.renerd.view_models.FiltersTabsListModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class EpisodesPresenter(private val repository: EpisodesContract.Repository) : EpisodesContract.Presenter {

    private var view: EpisodesContract.View? = null

    private var filtersTabsListModel = FiltersTabsListModel(
        productsList = mutableListOf(),
        subjectsList = mutableListOf(),
        guestsList = mutableListOf(),
        yearsList = mutableListOf()
    )

    override fun attachView(view: EpisodesContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }


    override fun getFiltersTabsList(){
        //view?.showLoading()
        try {
            CoroutineScope(Dispatchers.Main).launch {

                view?.showActionButtons(getFiltersTabsListModel())
            }
        }
        catch (_:Exception){

        }
    }



    override fun updateFiltersTabsList(tempFiltersTabsListModel:FiltersTabsListModel) {
        try {
            CoroutineScope(Dispatchers.Main).launch {
                for (item in tempFiltersTabsListModel.productsList) {
                    repository.insertFilterTabItem(item)
                }

                for (item in tempFiltersTabsListModel.subjectsList) {
                    repository.insertFilterTabItem(item)
                }

                for (item in tempFiltersTabsListModel.guestsList) {
                    repository.insertFilterTabItem(item)
                }

                for (item in tempFiltersTabsListModel.yearsList) {
                    repository.insertFilterTabItem(item)
                }


                view?.showActionButtons(getFiltersTabsListModel())
            }
        }
        catch (_:Exception){

        }
    }



    private suspend fun getFiltersTabsListModel(): FiltersTabsListModel{
        val episodes = repository.getEpisodes()

        val productsList = extractUniqueProducts(episodes)
        val subjectsList = extractUniqueSubjects(episodes)
        val guestsList = extractUniqueGuests(episodes)
        val yearsList = extractUniqueYears(episodes)


        for (product in productsList) {
            if (product.isNotEmpty()) {
                val tempList = FiltersTabsListItemModel(
                    label = product,
                    type = "product",
                    status = true
                )
                repository.insertFilterTabItem(tempList)
            }
        }


        for (subject in subjectsList) {
            if (subject.isNotEmpty()) {
                val tempList = FiltersTabsListItemModel(
                    label = subject,
                    type = "subject",
                    status = true
                )
                repository.insertFilterTabItem(tempList)
            }
        }


        for (guest in guestsList) {
            if (guest.isNotEmpty()) {
                val tempList = FiltersTabsListItemModel(
                    label = guest,
                    type = "guest",
                    status = true
                )
                repository.insertFilterTabItem(tempList)
            }
        }


        for (year in yearsList) {
            if (year.isNotEmpty()) {
                val tempList = FiltersTabsListItemModel(
                    label = year,
                    type = "year",
                    status = true
                )
                repository.insertFilterTabItem(tempList)
            }
        }


        val allMixedFilters = repository.getAllFilterTabItems()


        filtersTabsListModel = FiltersTabsListModel(
            productsList = mutableListOf(),
            subjectsList = mutableListOf(),
            guestsList = mutableListOf(),
            yearsList = mutableListOf()
        )

        for(item in allMixedFilters){
            when(item.type){
                "product" -> filtersTabsListModel.productsList.add(item)
                "subject" -> filtersTabsListModel.subjectsList.add(item)
                "guest" -> filtersTabsListModel.guestsList.add(item)
                "year" -> filtersTabsListModel.yearsList.add(item)
            }
        }

        return filtersTabsListModel
    }


    override fun loadEpisodes() {
        view?.showLoading()
        try {
            CoroutineScope(Dispatchers.Main).launch {
                val episodes = repository.getEpisodes()


                val filteredByProducts = filterEpisodesByProductsInclude(episodes, getLabelsWithStatusTrue(getFiltersTabsListModel().productsList)).toMutableList() ?: mutableListOf<EpisodeViewModel>()
                val filteredBySubjects = filterEpisodesBySubjectInclude(filteredByProducts, getLabelsWithStatusTrue(getFiltersTabsListModel().subjectsList)).toMutableList() ?: mutableListOf<EpisodeViewModel>()
                val filteredByGuests = filterEpisodesByGuestInclude(filteredBySubjects, getLabelsWithStatusTrue(getFiltersTabsListModel().guestsList)).toMutableList() ?: mutableListOf<EpisodeViewModel>()
                val filteredByYears = filterEpisodesByYearInclude(filteredByGuests, getLabelsWithStatusTrue(getFiltersTabsListModel().yearsList)).toMutableList() ?: mutableListOf<EpisodeViewModel>()


                view?.showEpisodes(filteredByYears)
                view?.hideLoading()
            }
        } catch (e: Exception) {
            view?.showError("Erro ao carregar episódios")
        }
    }




    private fun getLabelsWithStatusTrue(items: MutableList<FiltersTabsListItemModel>): List<String> {
        return items.filter { it.status }
            .map { it.label }
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

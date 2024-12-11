package com.example.renerd.features.episodes

import com.example.renerd.core.utils.log
import com.example.renerd.view_models.EpisodeViewModel
import com.example.renerd.view_models.FiltersTabsItemModel
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
        try {
            CoroutineScope(Dispatchers.Main).launch {
                val filtersTabsListModel = getFiltersTabsListModelFromRepository()
                view?.showActionButtons(filtersTabsListModel)
            }
        }
        catch (_:Exception){ }
    }


    override fun updateFiltersTabsItemList(mixedFiltersTabsItemModel:MutableList<FiltersTabsItemModel>){
        view?.showLoading()
        try {
            CoroutineScope(Dispatchers.Main).launch {
                for (item in mixedFiltersTabsItemModel) {
                    repository.updateFilterTabItem(item)
                }
            }
            this.loadEpisodes()
        }
        catch (_:Exception){}
    }



    override fun recyclerviewEpisodesCurrentPosition(currentPosition: Int){
        repository.setRecyclerviewEpisodesCurrentPosition(currentPosition)
    }



    private suspend fun getFiltersTabsListModelFromRepository(): FiltersTabsListModel{
        var allMixedFilters = repository.getAllFilterTabItems()

        if(allMixedFilters.isEmpty()){ //Isso irá inserir no BD todos os filtros
            val episodes = repository.getEpisodes()

            val productsList = extractUniqueProducts(episodes)
            val subjectsList = extractUniqueSubjects(episodes)
            val guestsList = extractUniqueGuests(episodes)
            val yearsList = extractUniqueYears(episodes)


            for (product in productsList) {
                if (product.isNotEmpty()) {
                    val tempList = FiltersTabsItemModel(
                        label = product,
                        type = "product",
                        status = true
                    )
                    repository.insertFilterTabItem(tempList)
                }
            }


            for (subject in subjectsList) {
                if (subject.isNotEmpty()) {
                    val tempList = FiltersTabsItemModel(
                        label = subject,
                        type = "subject",
                        status = true
                    )
                    repository.insertFilterTabItem(tempList)
                }
            }


            for (guest in guestsList) {
                if (guest.isNotEmpty()) {
                    val tempList = FiltersTabsItemModel(
                        label = guest,
                        type = "guest",
                        status = true
                    )
                    repository.insertFilterTabItem(tempList)
                }
            }


            for (year in yearsList) {
                if (year.isNotEmpty()) {
                    val tempList = FiltersTabsItemModel(
                        label = year,
                        type = "year",
                        status = true
                    )
                    repository.insertFilterTabItem(tempList)
                }
            }
        }


        //Para ele obter os IDs criados no DB
        allMixedFilters = repository.getAllFilterTabItems()


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


                view?.showActionButtons(getFiltersTabsListModelFromRepository())
            }
        }
        catch (_:Exception){

        }
    }








    override fun loadEpisodes() {
        view?.showLoading()
        try {
            CoroutineScope(Dispatchers.Main).launch {
                val episodes = repository.getEpisodes()

                val filteredByProducts = filterEpisodesByProductsInclude(episodes, getLabelsWithStatusTrue(getFiltersTabsListModelFromRepository().productsList)).toMutableList() ?: mutableListOf<EpisodeViewModel>()
                val filteredBySubjects = filterEpisodesBySubjectInclude(filteredByProducts, getLabelsWithStatusTrue(getFiltersTabsListModelFromRepository().subjectsList)).toMutableList() ?: mutableListOf<EpisodeViewModel>()
                val filteredByGuests = filterEpisodesByGuestInclude(filteredBySubjects, getLabelsWithStatusTrue(getFiltersTabsListModelFromRepository().guestsList)).toMutableList() ?: mutableListOf<EpisodeViewModel>()
                val filteredByYears = filterEpisodesByYearInclude(filteredByGuests, getLabelsWithStatusTrue(getFiltersTabsListModelFromRepository().yearsList)).toMutableList() ?: mutableListOf<EpisodeViewModel>()


                val recyclerviewEpisodesCurrentPosition = repository.getRecyclerviewEpisodesCurrentPosition()

                view?.showEpisodes(filteredByYears, recyclerviewEpisodesCurrentPosition.toInt())
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



                val recyclerviewEpisodesCurrentPosition = repository.getRecyclerviewEpisodesCurrentPosition()

                view?.showEpisodes(filteredByGuests, recyclerviewEpisodesCurrentPosition.toInt())
            }
        } catch (e: Exception) {
            view?.showError("Erro ao carregar episódios")
        }
    }














    private fun getLabelsWithStatusTrue(items: MutableList<FiltersTabsItemModel>): List<String> {
        return items.filter { it.status }.map { it.label }
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

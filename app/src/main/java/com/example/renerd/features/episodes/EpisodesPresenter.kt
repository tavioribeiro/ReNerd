package com.example.renerd.features.episodes

import com.example.renerd.core.utils.log
import com.example.renerd.view_models.EpisodeViewModel
import com.example.renerd.view_models.FiltersTabsItemModel
import com.example.renerd.view_models.FiltersTabsListModel
import com.example.renerd.features.episodes.utils.EpisodeFilterUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EpisodesPresenter(
    private val repository: EpisodesContract.Repository
): EpisodesContract.Presenter {

    private var view: EpisodesContract.View? = null
    private var filtersTabsListModel: FiltersTabsListModel = FiltersTabsListModel(
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

    override fun loadData() {
        view?.showLoading()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val filtersModel = loadOrInitializeFilters()

                val episodes = repository.getEpisodes()

                val activeProducts = EpisodeFilterUtil.getActiveLabels(filtersModel.productsList)
                val activeSubjects = EpisodeFilterUtil.getActiveLabels(filtersModel.subjectsList)
                val activeGuests = EpisodeFilterUtil.getActiveLabels(filtersModel.guestsList)
                val activeYears = EpisodeFilterUtil.getActiveLabels(filtersModel.yearsList)

                val filteredEpisodes = episodes
                    .let { EpisodeFilterUtil.filterEpisodesByProductsInclude(it, activeProducts) }
                    .let { EpisodeFilterUtil.filterEpisodesBySubjectInclude(it, activeSubjects) }
                    .let { EpisodeFilterUtil.filterEpisodesByGuestInclude(it, activeGuests) }
                    .let { EpisodeFilterUtil.filterEpisodesByYearInclude(it, activeYears) }
                    .toMutableList()
                val currentPosition = repository.getRecyclerviewEpisodesCurrentPosition().toIntOrNull() ?: 0


                view?.setListsData(tempFiltersTabsListModel = filtersModel, tempEpisodesList = filteredEpisodes)

                view?.showEpisodes(filteredEpisodes, currentPosition)
            } catch (e: Exception) {
                view?.showError("Erro ao carregar episódios")
                log(e)
            } finally {
                view?.hideLoading()
            }
        }
    }

    override fun updateFiltersTabsItemList(mixedFiltersTabsItemModel: MutableList<FiltersTabsItemModel>) {
        view?.showLoading()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                mixedFiltersTabsItemModel.forEach { repository.updateFilterTabItem(it) }
                // Após atualizar os filtros, recarrega os dados
                loadData()
            } catch (e: Exception) {
                log(e)
            }
        }
    }

    override fun recyclerviewEpisodesCurrentPosition(currentPosition: Int) {
        repository.setRecyclerviewEpisodesCurrentPosition(currentPosition)
    }

    private suspend fun loadOrInitializeFilters(): FiltersTabsListModel {
        var allMixedFilters = repository.getAllFilterTabItems()
        if (allMixedFilters.isEmpty()) {
            val episodes = repository.getEpisodes()
            val products = EpisodeFilterUtil.extractUniqueProducts(episodes)
            val subjects = EpisodeFilterUtil.extractUniqueSubjects(episodes)
            val guests = EpisodeFilterUtil.extractUniqueGuests(episodes)
            val years = EpisodeFilterUtil.extractUniqueYears(episodes)

            products.forEach { product ->
                if (product.isNotEmpty()) {
                    val item = FiltersTabsItemModel(label = product, type = "product", status = true)
                    repository.insertFilterTabItem(item)
                }
            }
            subjects.forEach { subject ->
                if (subject.isNotEmpty()) {
                    val item = FiltersTabsItemModel(label = subject, type = "subject", status = true)
                    repository.insertFilterTabItem(item)
                }
            }
            guests.forEach { guest ->
                if (guest.isNotEmpty()) {
                    val item = FiltersTabsItemModel(label = guest, type = "guest", status = true)
                    repository.insertFilterTabItem(item)
                }
            }
            years.forEach { year ->
                if (year.isNotEmpty()) {
                    val item = FiltersTabsItemModel(label = year, type = "year", status = true)
                    repository.insertFilterTabItem(item)
                }
            }
        }

        // Recarrega os filtros com os IDs gerados
        allMixedFilters = repository.getAllFilterTabItems()

        filtersTabsListModel = FiltersTabsListModel(
            productsList = mutableListOf(),
            subjectsList = mutableListOf(),
            guestsList = mutableListOf(),
            yearsList = mutableListOf()
        )
        allMixedFilters.forEach { item ->
            when (item.type) {
                "product" -> filtersTabsListModel.productsList.add(item)
                "subject" -> filtersTabsListModel.subjectsList.add(item)
                "guest" -> filtersTabsListModel.guestsList.add(item)
                "year" -> filtersTabsListModel.yearsList.add(item)
            }
        }
        return filtersTabsListModel
    }

    override fun updateFiltersTabsList(tempFiltersTabsListModel: FiltersTabsListModel) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                tempFiltersTabsListModel.productsList.forEach { repository.insertFilterTabItem(it) }
                tempFiltersTabsListModel.subjectsList.forEach { repository.insertFilterTabItem(it) }
                tempFiltersTabsListModel.guestsList.forEach { repository.insertFilterTabItem(it) }
                tempFiltersTabsListModel.yearsList.forEach { repository.insertFilterTabItem(it) }
            } catch (e: Exception) {
                log(e)
            }
        }
    }

    override fun loadLastEpisodes() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val episodes = repository.getLastEpisodes()
                val filteredEpisodes = episodes
                    .let { EpisodeFilterUtil.filterEpisodesByProductsInclude(it, listOf("nerdcast", "nerdtech")) }
                    .let { EpisodeFilterUtil.filterEpisodesBySubjectInclude(it, listOf("Ciências", "Cinema")) }
                    .let { EpisodeFilterUtil.filterEpisodesByGuestInclude(it, listOf("Affonso Solano")) }
                    .toMutableList()

                val currentPosition = repository.getRecyclerviewEpisodesCurrentPosition().toIntOrNull() ?: 0
                view?.showEpisodes(filteredEpisodes, currentPosition)
            } catch (e: Exception) {
                view?.showError("Erro ao carregar episódios")
                log(e)
            }
        }
    }
}

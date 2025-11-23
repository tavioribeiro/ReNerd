package com.podcast.renerd.features.episodes.utils

import com.podcast.renerd.view_models.EpisodeViewModel
import com.podcast.renerd.view_models.FiltersTabsItemModel

object EpisodeFilterUtil {

    fun extractUniqueProducts(episodes: List<EpisodeViewModel>): List<String> {
        return episodes.map { it.product }
            .distinct()
            .filter { it.isNotEmpty() }
    }

    fun extractUniqueSubjects(episodes: List<EpisodeViewModel>): List<String> {
        return episodes.flatMap { it.subject.split(",") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    fun extractUniqueGuests(episodes: List<EpisodeViewModel>): List<String> {
        return episodes.flatMap { it.guests.split(",") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    fun extractUniqueYears(episodes: List<EpisodeViewModel>): List<String> {
        return episodes.map { it.publishedAt.substring(0, 4) }
            .distinct()
            .sorted()
    }

    fun filterEpisodesByProductsInclude(episodes: List<EpisodeViewModel>, products: List<String>): List<EpisodeViewModel> {
        return episodes.filter { it.product in products }
    }

    fun filterEpisodesBySubjectInclude(episodes: List<EpisodeViewModel>, subjectsToInclude: List<String>): List<EpisodeViewModel> {
        val normalizedSubjects = subjectsToInclude.flatMap { it.split(",") }
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
        return episodes.filter { episode ->
            val episodeSubjects = episode.subject.split(",").map { it.trim().lowercase() }
            episodeSubjects.any { it in normalizedSubjects }
        }
    }

    fun filterEpisodesByGuestInclude(episodes: List<EpisodeViewModel>, guests: List<String>): List<EpisodeViewModel> {
        val normalizedGuests = guests.flatMap { it.split(",") }
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
        return episodes.filter { episode ->
            val episodeGuests = episode.guests.split(",").map { it.trim().lowercase() }
            episodeGuests.any { it in normalizedGuests }
        }
    }

    fun filterEpisodesByYearInclude(episodes: List<EpisodeViewModel>, years: List<String>): List<EpisodeViewModel> {
        return episodes.filter { it.publishedAt.substring(0, 4) in years }
    }

    fun getActiveLabels(filters: MutableList<FiltersTabsItemModel>): List<String> {
        return filters.filter { it.status }.map { it.label }
    }
}

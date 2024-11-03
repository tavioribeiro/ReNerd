package com.example.renerd.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.renerd.Database
import com.example.renerd.view_models.EpisodeViewModel
import com.example.renerd.view_models.FiltersTabsItemModel
import com.example.renerd.view_models.toEpisodeViewModel


class DatabaseHelper(context: Context) {
    private val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context, "episode.db")
    private val database: Database = Database(driver)




    // Pega todos os episódios
    fun getAllEpisodes(): List<EpisodeViewModel> {
        return database.episodeQueries.selectAllEpisodes().executeAsList().map { it.toEpisodeViewModel() }
    }

    // Pega um episódio por ID
    fun getEpisodeById(id: Long): EpisodeViewModel? { // Ajuste o tipo para Long
        return database.episodeQueries.selectEpisodeById(id).executeAsOneOrNull()?.toEpisodeViewModel()
    }

    // Insere um novo episódio
    fun insertEpisode(episode: EpisodeViewModel) {
        database.episodeQueries.insertEpisode(
            title = episode.title,
            description = episode.description,
            image_url = episode.imageUrl,
            audio_url = episode.audioUrl,
            duration = episode.duration,
            published_at = episode.publishedAt,
            slug = episode.slug,
            episode = episode.episode,
            product = episode.product,
            product_name = episode.productName,
            subject = episode.subject,
            jump_to_time = episode.jumpToTime,
            guests = episode.guests,
            post_type_class = episode.postTypeClass,
            elapsed_time = episode.elapsedTime
        )
    }

    // Deleta um episódio por ID
    fun deleteEpisodeById(id: Long) { // Ajuste o tipo para Long
        database.episodeQueries.deleteEpisodeById(id)
    }



    //******************************************************

    // Insere um novo FilterProductTabItem
    fun insertFilterTabItem(filterTabItem: FiltersTabsItemModel) {
        database.filterTabItemQueries.insertFilterTabItem(
            id = null,
            label = filterTabItem.label,
            type = filterTabItem.type,
            status = filterTabItem.status
        )
    }

    // Pega todos os FilterProductTabItem
    fun getAllFilterTabItems(): List<FiltersTabsItemModel> {
        return database.filterTabItemQueries.selectAllFilterTabItem().executeAsList().map { dbModel ->
            com.example.renerd.view_models.FiltersTabsItemModel(
                id = dbModel.id.toInt(),
                label = dbModel.label ?: "",
                type = dbModel.type ?: "",
                status = dbModel.status ?: true
            )
        }
    }

    // Insere um novo FilterProductTabItem
    fun updateFilterTabItem(filterTabItem: FiltersTabsItemModel) {
        filterTabItem.id?.let {
            database.filterTabItemQueries.updateFilterTabItem(
                id = it.toLong(),
                label = filterTabItem.label,
                type = filterTabItem.type,
                status = filterTabItem.status
            )
        }
    }

    //******************************************************

    // Pega todos os FilterProductTabItem
    fun getAllFilterProductTabItems(): List<FiltersTabsItemModel> {
        return database.filterProductTabItemQueries.selectAllFilterProductTabItem().executeAsList().map { it as FiltersTabsItemModel }
    }

    // Insere um novo FilterProductTabItem
    fun insertFilterProductTabItem(filterProductTabItem: FiltersTabsItemModel) {
        database.filterProductTabItemQueries.insertFilterProductTabItem(
            label = filterProductTabItem.label,
            status = filterProductTabItem.status
        )
    }


    //******************************************************

    // Pega todos os FilterSubjectTabItem
    fun getAllFilterSubjectTabItems(): List<FiltersTabsItemModel> {
        return database.filterSubjectTabItemQueries.selectAllFilterSubjectTabItem().executeAsList().map { it as FiltersTabsItemModel}
    }

    // Insere um novo FilterSubjectTabItem
    fun insertFilterSubjectTabItem(filterSubjectTabItem: FiltersTabsItemModel) {
        database.filterSubjectTabItemQueries.insertFilterSubjectTabItem(
            label = filterSubjectTabItem.label,
            status = filterSubjectTabItem.status
        )
    }


    //******************************************************


    fun getAllFilterGuestTabItems(): List<FiltersTabsItemModel> {
        return database.filterGuestTabItemQueries.selectAllFilterGuestTabItem().executeAsList().map { it as FiltersTabsItemModel }
    }

    // Insere um novo FilterGuestTabItem
    fun insertFilterGuestTabItem(filterGuestTabItem: FiltersTabsItemModel) {
        database.filterGuestTabItemQueries.insertFilterGuestTabItem(
            label = filterGuestTabItem.label,
            status = filterGuestTabItem.status
        )
    }

    //******************************************************


    // Pega todos os FilterYearTabItem
    fun getAllFilterYearTabItems(): List<FiltersTabsItemModel> {
        return database.filterYearTabItemQueries.selectAllFilterYearTabItem().executeAsList().map { it as FiltersTabsItemModel }
    }

    // Insere um novo FilterYearTabItem
    fun insertFilterYearTabItem(filterYearTabItem: FiltersTabsItemModel) {
        database.filterYearTabItemQueries.insertFilterYearTabItem(
            label = filterYearTabItem.label,
            status = filterYearTabItem.status
        )
    }


    //******************************************************

}

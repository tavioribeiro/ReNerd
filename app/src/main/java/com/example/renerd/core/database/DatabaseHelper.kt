package com.example.renerd.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.renerd.Database
import com.example.renerd.view_models.EpisodeViewModel
import com.example.renerd.view_models.FiltersTabsListItemModel
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
    fun insertFilterTabItem(filterTabItem: FiltersTabsListItemModel) {
        database.filterTabItemQueries.insertFilterTabItem(
            label = filterTabItem.label,
            type = filterTabItem.type,
            status = filterTabItem.status
        )
    }

    // Pega todos os FilterProductTabItem
    fun getAllFilterTabItems(): List<FiltersTabsListItemModel> {
        return database.filterTabItemQueries.selectAllFilterTabItem().executeAsList().map { dbModel ->
            com.example.renerd.view_models.FiltersTabsListItemModel(
                label = dbModel.label,
                type = dbModel.type ?: "",
                status = dbModel.status ?: true
            )
        }
    }

    //******************************************************

    // Pega todos os FilterProductTabItem
    fun getAllFilterProductTabItems(): List<FiltersTabsListItemModel> {
        return database.filterProductTabItemQueries.selectAllFilterProductTabItem().executeAsList().map { it as FiltersTabsListItemModel }
    }

    // Insere um novo FilterProductTabItem
    fun insertFilterProductTabItem(filterProductTabItem: FiltersTabsListItemModel) {
        database.filterProductTabItemQueries.insertFilterProductTabItem(
            label = filterProductTabItem.label,
            status = filterProductTabItem.status
        )
    }


    //******************************************************

    // Pega todos os FilterSubjectTabItem
    fun getAllFilterSubjectTabItems(): List<FiltersTabsListItemModel> {
        return database.filterSubjectTabItemQueries.selectAllFilterSubjectTabItem().executeAsList().map { it as FiltersTabsListItemModel}
    }

    // Insere um novo FilterSubjectTabItem
    fun insertFilterSubjectTabItem(filterSubjectTabItem: FiltersTabsListItemModel) {
        database.filterSubjectTabItemQueries.insertFilterSubjectTabItem(
            label = filterSubjectTabItem.label,
            status = filterSubjectTabItem.status
        )
    }


    //******************************************************


    fun getAllFilterGuestTabItems(): List<FiltersTabsListItemModel> {
        return database.filterGuestTabItemQueries.selectAllFilterGuestTabItem().executeAsList().map { it as FiltersTabsListItemModel }
    }

    // Insere um novo FilterGuestTabItem
    fun insertFilterGuestTabItem(filterGuestTabItem: FiltersTabsListItemModel) {
        database.filterGuestTabItemQueries.insertFilterGuestTabItem(
            label = filterGuestTabItem.label,
            status = filterGuestTabItem.status
        )
    }

    //******************************************************


    // Pega todos os FilterYearTabItem
    fun getAllFilterYearTabItems(): List<FiltersTabsListItemModel> {
        return database.filterYearTabItemQueries.selectAllFilterYearTabItem().executeAsList().map { it as FiltersTabsListItemModel }
    }

    // Insere um novo FilterYearTabItem
    fun insertFilterYearTabItem(filterYearTabItem: FiltersTabsListItemModel) {
        database.filterYearTabItemQueries.insertFilterYearTabItem(
            label = filterYearTabItem.label,
            status = filterYearTabItem.status
        )
    }


    //******************************************************

}

package com.example.renerd.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.renerd.Database
import com.example.renerd.view_models.EpisodeViewModel
import com.example.renerd.view_models.FiltersTabsItemModel
import com.example.renerd.view_models.toEpisodeViewModel


class DatabaseHelper(context: Context) {
    private val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context, "renerd.db")
    private val database: Database = Database(driver)


    // Pega todos os episódios
    fun getAllEpisodes(): List<EpisodeViewModel> {
        return database.episodeQueries.selectAllEpisodes().executeAsList().map { it.toEpisodeViewModel() }
    }

    // Pega um episódio por ID
    fun getEpisodeById(id: Long): EpisodeViewModel? {
        return database.episodeQueries.selectEpisodeById(id).executeAsOneOrNull()?.toEpisodeViewModel()
    }

    // Insere um novo episódio
    fun insertEpisode(episode: EpisodeViewModel) {
        database.episodeQueries.insertEpisode(
            title = episode.title,
            description = episode.description,
            image_url = episode.imageUrl,
            audio_url = episode.audioUrl,
            duration = episode.duration.toLong(),
            published_at = episode.publishedAt,
            slug = episode.slug,
            episode = episode.episode,
            product = episode.product,
            product_name = episode.productName,
            subject = episode.subject,
            jump_to_time = episode.jumpToTime.toLong(),
            guests = episode.guests,
            post_type_class = episode.postTypeClass,
            elapsed_time = episode.elapsedTime.toLong()
        )
    }

    // Deleta um episódio por ID
    fun deleteEpisodeById(id: Long) {
        database.episodeQueries.deleteEpisodeById(id)
    }


    // Atualiza um episódio existente
    fun updateEpisode(episode: EpisodeViewModel) {
        database.episodeQueries.updateEpisode(
            title = episode.title,
            description = episode.description,
            image_url = episode.imageUrl,
            audio_url = episode.audioUrl,
            duration = episode.duration.toLong(),
            published_at = episode.publishedAt,
            slug = episode.slug,
            episode = episode.episode,
            product = episode.product,
            product_name = episode.productName,
            subject = episode.subject,
            jump_to_time = episode.jumpToTime.toLong(),
            guests = episode.guests,
            post_type_class = episode.postTypeClass,
            elapsed_time = episode.elapsedTime.toLong(),
            id = episode.id.toLong()
        )
    }

    // Verifica se um episódio existe pelo ID
    fun episodeExists(id: Long): Boolean {
        return database.episodeQueries.selectEpisodeById(id).executeAsOneOrNull() != null
    }

    // Limpa todos os episódios do banco de dados
    fun deleteAllEpisodes() {
        database.episodeQueries.deleteAllEpisodes()
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
            FiltersTabsItemModel(
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
}
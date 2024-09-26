package com.example.renerd.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.renerd.Database
import com.example.renerd.view_models.EpisodeViewModel
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
}

package com.example.renerd.features.episodes.components.last_episodes_dialog

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.renerd.core.database.DatabaseHelper
import com.example.renerd.core.extentions.ContextManager
import com.example.renerd.core.network.PodcastClient
import com.example.renerd.core.utils.getCurrentDateFormatted
import com.example.renerd.core.utils.log
import com.example.renerd.view_models.EpisodeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.URLDecoder

class LastEpisodesDialogRepository : LastEpisodesDialogContract.Repository {

    private val context: Context = ContextManager.getGlobalContext()
    private val dbHelper = DatabaseHelper(context)
    private val sharedPref = context.getSharedPreferences("SharedPrefsReNerd", Context.MODE_PRIVATE)
    private val editor = sharedPref.edit()

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun fetchLastEpisodesSinceLastUpdate(): List<EpisodeViewModel> {
        return withContext(Dispatchers.IO) {
            try {
                val after = URLDecoder.decode(getAfterDate(), "UTF-8")
                val response = PodcastClient.api.getNerdcasts(after = after, before = "").execute()
                val newEpisodes = mutableListOf<EpisodeViewModel>()

                if (response.isSuccessful) {
                    response.body()?.forEach { episode ->
                        val episodeViewModel = EpisodeViewModel(
                            id = episode.id,
                            title = episode.title ?: "",
                            description = episode.description ?: "",
                            imageUrl = episode.image ?: "",
                            audioUrl = episode.audioHigh ?: "",
                            duration = episode.duration,
                            publishedAt = episode.publishedAt ?: "",
                            slug = episode.slug ?: "",
                            episode = episode.episode ?: "",
                            product = episode.product ?: "",
                            productName = episode.productName ?: "",
                            subject = episode.subject ?: "",
                            jumpToTime = episode.jumpToTime.startTime,
                            guests = episode.guests ?: "",
                            postTypeClass = episode.postTypeClass ?: ""
                        )
                        newEpisodes.add(episodeViewModel)
                    }
                }
                newEpisodes
            } catch (e: SocketTimeoutException) {
                log(e)
                mutableListOf()
            } catch (e: Exception) {
                log(e)
                mutableListOf()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun saveNewEpisodes(episodes: List<EpisodeViewModel>) {
        withContext(Dispatchers.IO) {
            try {
                episodes.forEach { episode ->
                    dbHelper.insertEpisode(episode)
                }
                updateAfterDate()
            } catch (e: Exception) {
                log(e)
            }
        }
    }

    private fun getAfterDate(): String {
        var after = sharedPref.getString("current_after_search", "") ?: ""
        if (after.isEmpty()) {
            after = "2000-01-01%2000%3A00%3A00"
        }
        return after
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAfterDate() {
        editor.putString("current_after_search", getCurrentDateFormatted())
        editor.apply()
    }
}
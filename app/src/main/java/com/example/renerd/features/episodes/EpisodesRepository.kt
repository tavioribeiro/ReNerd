package com.example.renerd.features.episodes

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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.URLDecoder
import kotlin.random.Random

class EpisodesRepository: EpisodesContract.Repository {
    val context = ContextManager.getGlobalContext()

    val sharedPref = context.getSharedPreferences("SharedPrefsReNerd", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()

    val dbHelper = DatabaseHelper(context)


    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getEpisodes(): MutableList<EpisodeViewModel> {
        return withContext(Dispatchers.IO) {
            try {
                val localEpisodes = dbHelper.getAllEpisodes().toMutableList()
                if (localEpisodes.isNotEmpty()) {
                    return@withContext localEpisodes
                }


                val after = getAfter()
                //val before = "2024-09-13%2000%3A00%3A00"

                val afterDecoded = URLDecoder.decode(after, "UTF-8")
                //val beforeDecoded = URLDecoder.decode(before, "UTF-8")
                //val response = PodcastClient.api.getNerdcasts(after = afterDecoded, before = beforeDecoded).execute()
                val response = PodcastClient.api.getNerdcasts(after = afterDecoded, before = "").execute()

                if (response.isSuccessful) {
                    val podcasts = response.body()
                    val episodesViewModel = mutableListOf<EpisodeViewModel>()
                    if (!podcasts.isNullOrEmpty()) {
                        for (episode in podcasts) {
                            val episodeViewModel = EpisodeViewModel(
                                id = episode.id.toLong(),
                                title = episode.title ?: "",
                                description = episode.description ?: "",
                                imageUrl = episode.image ?: "",
                                audioUrl = episode.audioHigh ?: "",
                                duration = episode.duration.toLong(),
                                publishedAt = episode.publishedAt ?: "",
                                slug = episode.slug ?: "",
                                episode = episode.episode ?: "",
                                product = episode.product ?: "",
                                productName = episode.productName ?: "",
                                subject = episode.subject ?: "",
                                jumpToTime = episode.jumpToTime.startTime.toLong(),
                                guests = episode.guests ?: "",
                                postTypeClass = episode.postTypeClass ?: "",
                            )

                            dbHelper.insertEpisode(episodeViewModel)
                            episodesViewModel.add(episodeViewModel)
                        }
                    }
                    setAfter()
                    episodesViewModel
                } else {
                    mutableListOf()
                }
            } catch (e: SocketTimeoutException) {
                log(e)
                mutableListOf()
            } catch (e: Exception) {
                log(e)
                mutableListOf()
            }
        }
    }

    private fun getAfter(): String {
        var current_after_search = sharedPref.getString("current_after_search", "") ?: ""

        if(current_after_search == ""){
            current_after_search = "2000-01-01%2000%3A00%3A00"
        }

        return current_after_search
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAfter() {
        editor.putString("current_after_search", getCurrentDateFormatted())
        editor.apply()
    }
}


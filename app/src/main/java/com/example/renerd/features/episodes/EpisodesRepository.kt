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
import com.example.renerd.view_models.FiltersTabsItemModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.URLDecoder

class EpisodesRepository : EpisodesContract.Repository {
    private val context: Context = ContextManager.getGlobalContext()
    private val sharedPref = context.getSharedPreferences("SharedPrefsReNerd", Context.MODE_PRIVATE)
    private val editor = sharedPref.edit()
    private val dbHelper = DatabaseHelper(context)

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getEpisodes(): MutableList<EpisodeViewModel> {
        return withContext(Dispatchers.IO) {
            try {
                val localEpisodes = dbHelper.getAllEpisodes().toMutableList()
                if (localEpisodes.isNotEmpty()) return@withContext localEpisodes

                fetchEpisodesFromNetwork()
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
    private fun fetchEpisodesFromNetwork(): MutableList<EpisodeViewModel> {
        val after = URLDecoder.decode(getAfter(), "UTF-8")
        val response = PodcastClient.api.getNerdcasts(after = after, before = "").execute()
        val episodesViewModel = mutableListOf<EpisodeViewModel>()
        if (response.isSuccessful) {
            val podcasts = response.body()
            podcasts?.forEach { episode ->
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
                    jumpToTime = episode.jumpToTime.endTime,
                    guests = episode.guests ?: "",
                    postTypeClass = episode.postTypeClass ?: ""
                )
                dbHelper.insertEpisode(episodeViewModel)
                episodesViewModel.add(episodeViewModel)
            }
            setAfter()
        }
        return episodesViewModel
    }

    override suspend fun insertFilterTabItem(filtersTabsItemModel: FiltersTabsItemModel) {
        withContext(Dispatchers.IO) {
            try {
                dbHelper.insertFilterTabItem(filterTabItem = filtersTabsItemModel)
            } catch (e: SocketTimeoutException) {
                log(e)
            }
        }
    }

    override suspend fun getAllFilterTabItems(): MutableList<FiltersTabsItemModel> {
        return withContext(Dispatchers.IO) {
            try {
                val localFilterTabItems = dbHelper.getAllFilterTabItems().toMutableList()
                if (localFilterTabItems.isNotEmpty()) return@withContext localFilterTabItems
                mutableListOf()
            } catch (e: SocketTimeoutException) {
                log(e)
                mutableListOf()
            }
        }
    }

    override suspend fun updateFilterTabItem(filtersTabsItemModel: FiltersTabsItemModel) {
        withContext(Dispatchers.IO) {
            try {
                dbHelper.updateFilterTabItem(filterTabItem = filtersTabsItemModel)
            } catch (e: SocketTimeoutException) {
                log(e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getLastEpisodes(): MutableList<EpisodeViewModel> {
        return withContext(Dispatchers.IO) {
            try {
                val localEpisodes = dbHelper.getAllEpisodes().toMutableList()
                val after = URLDecoder.decode(getAfter(), "UTF-8")
                val response = PodcastClient.api.getNerdcasts(after = after, before = "").execute()
                if (response.isSuccessful) {
                    val podcasts = response.body()
                    podcasts?.forEach { episode ->
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
                        dbHelper.insertEpisode(episodeViewModel)
                        localEpisodes.add(episodeViewModel)
                    }
                    this@EpisodesRepository.setAfter()
                }
                localEpisodes
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
        var after = sharedPref.getString("current_after_search", "") ?: ""
        if (after.isEmpty()) {
            after = "2000-01-01%2000%3A00%3A00"
        }
        return after
    }

    override fun setRecyclerviewEpisodesCurrentPosition(currentPosition: Int) {
        editor.putString("recyclerviewEpisodesCurrentPosition", currentPosition.toString())
        editor.apply()
    }

    override fun getRecyclerviewEpisodesCurrentPosition(): String {
        return sharedPref.getString("recyclerviewEpisodesCurrentPosition", "0") ?: "0"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAfter() {
        editor.putString("current_after_search", getCurrentDateFormatted())
        editor.apply()
    }
}

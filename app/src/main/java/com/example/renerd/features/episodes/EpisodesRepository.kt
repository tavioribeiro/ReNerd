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
                val afterDecoded = URLDecoder.decode(after, "UTF-8")
                val response = PodcastClient.api.getNerdcasts(after = afterDecoded, before = "").execute()

                if (response.isSuccessful) {
                    val podcasts = response.body()
                    val episodesViewModel = mutableListOf<EpisodeViewModel>()
                    if (!podcasts.isNullOrEmpty()) {
                        for (episode in podcasts) {
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
                if (localFilterTabItems.isNotEmpty()) {
                    return@withContext localFilterTabItems
                }

                return@withContext mutableListOf()
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











/*



    override suspend fun insertFilterProductTabItem(filtersTabsListItemModel: FiltersTabsListItemModel) {
        withContext(Dispatchers.IO) {
            try {
                dbHelper.insertFilterProductTabItem(filterProductTabItem = filtersTabsListItemModel)
                //log(filtersTabsListItemModel.label)
            } catch (e: SocketTimeoutException) {
                log(e)
            }
        }
    }

    override suspend fun getAllFilterProductTabItems(): MutableList<FiltersTabsListItemModel> {
        return withContext(Dispatchers.IO) {
            try {
                val localFilterProductTabItems = dbHelper.getAllFilterProductTabItems().toMutableList()
                if (localFilterProductTabItems.isNotEmpty()) {
                    return@withContext localFilterProductTabItems
                }

                return@withContext mutableListOf()
            } catch (e: SocketTimeoutException) {
                log(e)
                mutableListOf()
            }
        }
    }

    override suspend fun insertFilterSubjectTabItem(filtersTabsListItemModel: FiltersTabsListItemModel) {
        withContext(Dispatchers.IO) {
            try {
                dbHelper.insertFilterSubjectTabItem(filterSubjectTabItem = filtersTabsListItemModel)
                //log(filtersTabsListItemModel.label)
            } catch (e: SocketTimeoutException) {
                log(e)
            }
        }
    }

    override suspend fun getAllFilterSubjectTabItems(): MutableList<FiltersTabsListItemModel> {
        return withContext(Dispatchers.IO) {
            try {
                val localFilterSubjectTabItems = dbHelper.getAllFilterSubjectTabItems().toMutableList()
                if (localFilterSubjectTabItems.isNotEmpty()) {
                    return@withContext localFilterSubjectTabItems
                }

                return@withContext mutableListOf()
            } catch (e: SocketTimeoutException) {
                log(e)
                mutableListOf()
            }
        }
    }

    override suspend fun insertFilterGuestTabItem(filtersTabsListItemModel: FiltersTabsListItemModel) {
        withContext(Dispatchers.IO) {
            try {
                dbHelper.insertFilterGuestTabItem(filterGuestTabItem = filtersTabsListItemModel)
                //log(filtersTabsListItemModel.label)
            } catch (e: SocketTimeoutException) {
                log(e)
            }
        }
    }

    override suspend fun getAllFilterGuestTabItems(): MutableList<FiltersTabsListItemModel> {
        return withContext(Dispatchers.IO) {
            try {
                val localFilterGuestTabItems = dbHelper.getAllFilterGuestTabItems().toMutableList()
                if (localFilterGuestTabItems.isNotEmpty()) {
                    return@withContext localFilterGuestTabItems
                }

                return@withContext mutableListOf()
            } catch (e: SocketTimeoutException) {
                log(e)
                mutableListOf()
            }
        }
    }


    override suspend fun insertFilterYearTabItem(filtersTabsListItemModel: FiltersTabsListItemModel) {
        withContext(Dispatchers.IO) {
            try {
                dbHelper.insertFilterYearTabItem(filterYearTabItem = filtersTabsListItemModel)
                //log(filtersTabsListItemModel.label)
            } catch (e: SocketTimeoutException) {
                log(e)
            }
        }
    }


    override suspend fun getAllFilterYearTabItems(): MutableList<FiltersTabsListItemModel> {
        return withContext(Dispatchers.IO) {
            try {
                val localFilterYearTabItems = dbHelper.getAllFilterYearTabItems().toMutableList()
                if (localFilterYearTabItems.isNotEmpty()) {
                    return@withContext localFilterYearTabItems
                }

                return@withContext mutableListOf()
            } catch (e: SocketTimeoutException) {
                log(e)
                mutableListOf()
            }
        }
    }


*/
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getLastEpisodes(): MutableList<EpisodeViewModel> {
        return withContext(Dispatchers.IO) {
            try {
                val localEpisodes = dbHelper.getAllEpisodes().toMutableList()

                val after = getAfter()
                val afterDecoded = URLDecoder.decode(after, "UTF-8")
                val response = PodcastClient.api.getNerdcasts(after = afterDecoded, before = "").execute()

                if (response.isSuccessful) {
                    val podcasts = response.body()

                    if (!podcasts.isNullOrEmpty()) {
                        for (episode in podcasts) {
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
                                postTypeClass = episode.postTypeClass ?: "",
                            )

                            dbHelper.insertEpisode(episodeViewModel)
                            localEpisodes.add(episodeViewModel)
                        }
                    }
                    setAfter()
                    localEpisodes
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


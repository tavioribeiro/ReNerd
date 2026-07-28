package com.podcast.renerd.features.episodes.components.search_dialog

import android.content.Context
import com.podcast.renerd.core.database.DatabaseHelper
import com.podcast.renerd.core.singletons.ContextManager
import com.podcast.renerd.view_models.EpisodeViewModel

class SearchDialogRepository : SearchDialogContract.Repository {

    private val context: Context = ContextManager.getContext()
    private val dbHelper = DatabaseHelper(context)

    override suspend fun getEpisodes(): List<EpisodeViewModel> {
        return dbHelper.getAllEpisodes()
    }
}

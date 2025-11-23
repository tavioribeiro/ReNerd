package com.podcast.renerd.features.episodes.components.floating_player

import android.content.Context
import com.podcast.renerd.core.database.DatabaseHelper
import com.podcast.renerd.core.singletons.ContextManager
import com.podcast.renerd.view_models.EpisodeViewModel


class FloatingPlayerRepository: FloatingPlayerContract.Repository {
    val context = ContextManager.getContext()

    val sharedPref = context.getSharedPreferences("SharedPrefsReNerd", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()
    val dbHelper = DatabaseHelper(context)



    override fun setCurrentEpisodePlayingId(id: Int){
        editor.putString("current_media_playing_id", id.toString())
        editor.apply()
    }

    override fun getCurrentEpisodePlayingId(): Int {
        val currentEpisodePlayingId = sharedPref.getString("current_media_playing_id", "")
        return if (currentEpisodePlayingId.isNullOrEmpty()) 0 else currentEpisodePlayingId.toInt()
    }



    //**************************

    override fun getEpisodeById(id: Long): EpisodeViewModel {
        return dbHelper.getEpisodeById(id) ?: EpisodeViewModel()
    }

    override fun updateEpisode(episode: EpisodeViewModel){
        dbHelper.updateEpisode(episode)
    }
}

















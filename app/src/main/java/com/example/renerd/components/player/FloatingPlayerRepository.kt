package com.example.renerd.components.player

import android.content.Context
import com.example.renerd.core.database.DatabaseHelper
import com.example.renerd.core.extentions.ContextManager
import com.example.renerd.view_models.EpisodeViewModel


class FloatingPlayerRepository: FloatingPlayerContract.Repository {
    val context = ContextManager.getGlobalContext()

    val sharedPref = context.getSharedPreferences("SharedPrefsReNerd", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()
    val dbHelper = DatabaseHelper(context)



    override suspend fun setCurrentEpisodePlaying(url: String){
        editor.putString("current_media_playing", url)
        editor.apply()
    }

    override fun getCurrentEpisodePlaying(): String{
        return sharedPref.getString("current_media_playing", "") ?: ""
    }

    override fun setCurrentEpisodePosition(url: String) {
        TODO("Not yet implemented")
    }

    override fun getCurrentEpisodePosition(): String {
        TODO("Not yet implemented")
    }



    //**************************

    override fun getEpisodeById(id: Long): EpisodeViewModel {
        return dbHelper.getEpisodeById(id) ?: EpisodeViewModel()
    }

    override fun updateEpisode(episode: EpisodeViewModel){
        dbHelper.updateEpisode(episode)
    }
}

















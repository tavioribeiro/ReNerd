package com.example.renerd.features.player

import android.content.Context


class PlayerRepository(private var context: Context): PlayerContract.Repository {
    //private lateinit var context: Context
    val sharedPref = context.getSharedPreferences("SharedPrefsReNerd", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()

    override fun setContext(context: Context) {

    }


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
}

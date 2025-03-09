package com.example.renerd.app

import com.example.renerd.features.episodes.components.floating_player.di.FloatingPlayerModule
import com.example.renerd.features.episodes.di.EpisodesModule
import com.example.renerd.features.player.di.PlayerModule


object MainModule {
    var instance = listOf(
        EpisodesModule.instance,
        PlayerModule.instance,
        FloatingPlayerModule.instance
    )
}

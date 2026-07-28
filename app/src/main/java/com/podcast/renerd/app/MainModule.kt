package com.podcast.renerd.app

import com.podcast.renerd.core.network.di.NetworkModule
import com.podcast.renerd.features.episodes.components.floating_player.di.FloatingPlayerModule
import com.podcast.renerd.features.episodes.di.EpisodesModule
import com.podcast.renerd.features.player.di.PlayerModule


object MainModule {
    var instance = listOf(
        NetworkModule.instance,
        EpisodesModule.instance,
        PlayerModule.instance,
        FloatingPlayerModule.instance
    )
}

package com.example.renerd.app

import com.example.renerd.features.episodes.di.EpisodesModule


object MainModule {
    var instance = listOf(
        EpisodesModule.instance
    )
}

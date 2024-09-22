package com.example.renerd

import com.example.renerd.features.episodes.di.EpisodesModule


object MainModule {
    var instance = listOf(
        EpisodesModule.instance
    )
}

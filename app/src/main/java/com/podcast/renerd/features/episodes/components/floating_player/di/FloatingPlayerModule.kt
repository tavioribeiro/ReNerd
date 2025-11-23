package com.podcast.renerd.features.episodes.components.floating_player.di


import com.podcast.renerd.features.episodes.components.floating_player.FloatingPlayerContract
import com.podcast.renerd.features.episodes.components.floating_player.FloatingPlayerPresenter
import com.podcast.renerd.features.episodes.components.floating_player.FloatingPlayerRepository
import org.koin.dsl.module


object FloatingPlayerModule {

    val instance = module {
        factory<FloatingPlayerContract.Repository> { FloatingPlayerRepository() }
        factory<FloatingPlayerContract.Presenter> { FloatingPlayerPresenter(get()) }
    }
}
package com.podcast.renerd.features.player.di


import com.podcast.renerd.features.player.PlayerContract
import com.podcast.renerd.features.player.PlayerPresenter
import com.podcast.renerd.features.player.PlayerRepository
import org.koin.dsl.module


object PlayerModule {

    val instance = module {
        factory<PlayerContract.Repository> { PlayerRepository() }
        factory<PlayerContract.Presenter> { PlayerPresenter(get()) }
    }
}
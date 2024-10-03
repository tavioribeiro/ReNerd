package com.example.renerd.features.player.di


import com.example.renerd.features.player.PlayerContract
import com.example.renerd.features.player.PlayerPresenter
import com.example.renerd.features.player.PlayerRepository
import org.koin.dsl.module


object PlayerModule {

    val instance = module {
        factory<PlayerContract.Repository> { PlayerRepository() }
        factory<PlayerContract.Presenter> { PlayerPresenter(get()) }
    }
}
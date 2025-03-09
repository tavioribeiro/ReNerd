package com.example.renerd.components.floating_player.di


import com.example.renerd.components.floating_player.FloatingPlayerContract
import com.example.renerd.components.floating_player.FloatingPlayerPresenter
import com.example.renerd.components.floating_player.FloatingPlayerRepository
import org.koin.dsl.module


object FloatingPlayerModule {

    val instance = module {
        factory<FloatingPlayerContract.Repository> { FloatingPlayerRepository() }
        factory<FloatingPlayerContract.Presenter> { FloatingPlayerPresenter(get()) }
    }
}